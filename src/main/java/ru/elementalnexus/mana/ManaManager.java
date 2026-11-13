package ru.elementalnexus.mana;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.event.PlayerManaChangeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Управляет маной игроков: хранение по UUID, восстановление,
 * периодическое сохранение в players.yml, события изменения маны.
 *
 * Все методы, изменяющие ману, должны вызываться из основного потока
 * сервера, потому что они вызывают событие PlayerManaChangeEvent.
 */
public class ManaManager {

    private final ElementalNexusPlugin plugin;
    private final Map<UUID, Double> mana = new HashMap<>();

    private final File dataFile;
    private YamlConfiguration dataConfig;

    private BukkitTask regenTask;
    private BukkitTask saveTask;

    public ManaManager(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
    }

    /**
     * Загружает player'овские данные из players.yml.
     */
    public void load() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        mana.clear();
        int loaded = 0;
        ConfigurationSection section = dataConfig.getConfigurationSection("players");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    mana.put(uuid, clamp(section.getDouble(key + ".mana", plugin.getNexusConfig().getInitialMana())));
                    loaded++;
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("[ElementalNexus] Некорректный UUID '" + key
                            + "' в players.yml — запись пропущена.");
                }
            }
        }
        plugin.getLogger().info("Данные маны загружены для " + loaded + " игроков.");
        // На случай перезагрузки плагина без рестарта сервера:
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player);
        }
    }

    /**
     * Сохраняет ману всех игроков в players.yml.
     */
    public void save() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, Double> entry : mana.entrySet()) {
            dataConfig.set("players." + entry.getKey() + ".mana", round(entry.getValue()));
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("[ElementalNexus] Не удалось сохранить players.yml: " + exception.getMessage());
        }
    }

    /**
     * Вызывается при входе игрока: подгружает сохранённую ману
     * или выдаёт начальное значение.
     */
    public void loadPlayer(Player player) {
        String path = "players." + player.getUniqueId() + ".mana";
        if (dataConfig != null && dataConfig.isSet(path)) {
            mana.put(player.getUniqueId(), clamp(dataConfig.getDouble(path)));
        } else {
            mana.put(player.getUniqueId(), clamp(plugin.getNexusConfig().getInitialMana()));
        }
    }

    /**
     * Вызывается при выходе игрока: сразу сохраняет его данные на диск.
     */
    public void unloadPlayer(Player player) {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }
        Double value = mana.remove(player.getUniqueId());
        if (value != null) {
            dataConfig.set("players." + player.getUniqueId() + ".mana", round(value));
            try {
                dataConfig.save(dataFile);
            } catch (IOException exception) {
                plugin.getLogger().severe("[ElementalNexus] Не удалось сохранить players.yml: " + exception.getMessage());
            }
        }
    }

    /**
     * Запускает задачи восстановления маны и периодического сохранения.
     * Перед запуском отменяет прежние задачи, чтобы при перезагрузке
     * конфигурации они не дублировались.
     */
    public void startTasks() {
        stopTasks();

        regenTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            double regen = plugin.getNexusConfig().getRegenPerSecond();
            double max = plugin.getNexusConfig().getMaxMana();
            for (Player player : Bukkit.getOnlinePlayers()) {
                double current = getMana(player);
                if (current < max) {
                    changeMana(player, Math.min(max, current + regen), ManaChangeReason.REGENERATION);
                }
            }
        }, 20L, 20L);

        long intervalTicks = Math.max(1L, plugin.getNexusConfig().getSaveIntervalSeconds()) * 20L;
        saveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::save, intervalTicks, intervalTicks);
    }

    /**
     * Отменяет запущенные задачи восстановления и сохранения.
     */
    public void stopTasks() {
        if (regenTask != null) {
            regenTask.cancel();
            regenTask = null;
        }
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
    }

    /**
     * Вызывается при /magic reload: данные игроков сохраняются в памяти,
     * значения выше нового максимума обрезаются, задачи перезапускаются
     * (без дублирования).
     */
    public void onConfigReload() {
        double max = plugin.getNexusConfig().getMaxMana();
        for (Map.Entry<UUID, Double> entry : mana.entrySet()) {
            if (entry.getValue() > max) {
                entry.setValue(max);
            }
        }
        startTasks();
    }

    public double getMana(Player player) {
        Double value = mana.get(player.getUniqueId());
        return value != null ? value : plugin.getNexusConfig().getInitialMana();
    }

    public double getMaxMana() {
        return plugin.getNexusConfig().getMaxMana();
    }

    public boolean hasMana(Player player, double amount) {
        return getMana(player) >= amount;
    }

    /**
     * Устанавливает ману игрока. Перед изменением вызывает отменяемое
     * событие PlayerManaChangeEvent.
     *
     * @return true, если значение было изменено (событие не отменено)
     */
    public boolean setMana(Player player, double amount, ManaChangeReason reason) {
        return changeMana(player, amount, reason);
    }

    public boolean addMana(Player player, double amount, ManaChangeReason reason) {
        return changeMana(player, getMana(player) + amount, reason);
    }

    public boolean removeMana(Player player, double amount, ManaChangeReason reason) {
        return changeMana(player, getMana(player) - amount, reason);
    }

    private boolean changeMana(Player player, double rawNewValue, ManaChangeReason reason) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "[ElementalNexus] Ману можно изменять только в основном потоке сервера!");
        }
        double oldValue = getMana(player);
        double newValue = clamp(rawNewValue);
        if (Double.compare(oldValue, newValue) == 0) {
            return true;
        }

        PlayerManaChangeEvent event = new PlayerManaChangeEvent(player, oldValue, newValue, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        mana.put(player.getUniqueId(), clamp(event.getNewMana()));
        return true;
    }

    /**
     * Ограничивает значение диапазоном [0, максимум маны].
     */
    private double clamp(double value) {
        double max = plugin.getNexusConfig().getMaxMana();
        if (value < 0) {
            return 0;
        }
        return Math.min(value, max);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package ru.elementalnexus;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.elementalnexus.combo.ComboManager;
import ru.elementalnexus.command.MagicCommand;
import ru.elementalnexus.config.NexusConfig;
import ru.elementalnexus.listener.ManaListener;
import ru.elementalnexus.listener.ProjectileListener;
import ru.elementalnexus.mana.ManaManager;
import ru.elementalnexus.spell.SpellManager;

/**
 * Главный класс плагина ElementalNexus.
 *
 * Магическая система стихий для Paper 1.18.2: мана, заклинания
 * четырёх стихий и комбинации из двух стихий.
 */
public class ElementalNexusPlugin extends JavaPlugin {

    public static final String PERMISSION_USE = "elementalnexus.use";
    public static final String PERMISSION_ADMIN = "elementalnexus.admin";

    private NexusConfig nexusConfig;
    private ManaManager manaManager;
    private SpellManager spellManager;
    private ComboManager comboManager;

    private NamespacedKey fireballDamageKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        fireballDamageKey = new NamespacedKey(this, "fireball_damage");

        // Менеджеры заклинаний и комбинаций регистрируются до загрузки
        // конфигурации, чтобы конфиг мог проверить все известные ID.
        spellManager = new SpellManager(this);
        spellManager.registerDefaults();
        comboManager = new ComboManager(this);
        comboManager.registerDefaults();

        nexusConfig = new NexusConfig(this);
        nexusConfig.load(spellManager.getSpellIds(), comboManager.getComboIds());

        manaManager = new ManaManager(this);
        manaManager.load();
        manaManager.startTasks();

        MagicCommand magicCommand = new MagicCommand(this);
        PluginCommand command = getCommand("magic");
        if (command != null) {
            command.setExecutor(magicCommand);
            command.setTabCompleter(magicCommand);
        } else {
            getLogger().severe("Команда /magic не найдена в plugin.yml! Команды плагина не будут работать.");
        }

        Bukkit.getPluginManager().registerEvents(new ManaListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProjectileListener(this), this);

        getLogger().info("ElementalNexus включён. Зарегистрировано заклинаний: "
                + spellManager.getSpellIds().size()
                + ", комбинаций: " + comboManager.getComboIds().size() + ".");
    }

    @Override
    public void onDisable() {
        if (manaManager != null) {
            manaManager.stopTasks();
            manaManager.save();
        }
        // Страховка: отменяем все задачи, которые могли остаться запущенными.
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("ElementalNexus выключен. Данные игроков сохранены.");
    }

    /**
     * Полная перезагрузка конфигурации без потери данных игроков
     * и без дублирования задач (старые задачи сначала отменяются).
     */
    public void reloadNexusConfig() {
        nexusConfig.load(spellManager.getSpellIds(), comboManager.getComboIds());
        manaManager.onConfigReload();
        getLogger().info("Конфигурация ElementalNexus перезагружена.");
    }

    public NexusConfig getNexusConfig() {
        return nexusConfig;
    }

    public ManaManager getManaManager() {
        return manaManager;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    public ComboManager getComboManager() {
        return comboManager;
    }

    /**
     * Ключ метаданных, которым помечается снаряд заклинания «Огненный снаряд».
     * Значение ключа — наносимый урон (double).
     */
    public NamespacedKey getFireballDamageKey() {
        return fireballDamageKey;
    }
}

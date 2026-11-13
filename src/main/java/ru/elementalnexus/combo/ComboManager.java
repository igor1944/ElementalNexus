package ru.elementalnexus.combo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.impl.ChainDischargeCombo;
import ru.elementalnexus.combo.impl.FlameVortexCombo;
import ru.elementalnexus.combo.impl.FrostFogCombo;
import ru.elementalnexus.combo.impl.PlasmaStrikeCombo;
import ru.elementalnexus.combo.impl.SteamBlastCombo;
import ru.elementalnexus.combo.impl.StormGustCombo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.config.NexusConfig;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.event.ElementCombinationEvent;
import ru.elementalnexus.mana.ManaChangeReason;
import ru.elementalnexus.util.FormatUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Реестр комбинаций стихий и логика их использования.
 */
public class ComboManager {

    private final ElementalNexusPlugin plugin;

    private final Map<String, Combo> combos = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public ComboManager(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует все шесть комбинаций пар разных стихий.
     */
    public void registerDefaults() {
        register(new FlameVortexCombo(plugin));      // FIRE + WIND
        register(new SteamBlastCombo(plugin));       // FIRE + WATER
        register(new PlasmaStrikeCombo(plugin));     // FIRE + LIGHTNING
        register(new FrostFogCombo(plugin));         // WIND + WATER
        register(new StormGustCombo(plugin));        // WIND + LIGHTNING
        register(new ChainDischargeCombo(plugin));   // WATER + LIGHTNING
    }

    public void register(Combo combo) {
        if (combos.containsKey(combo.getId())) {
            plugin.getLogger().warning("[ElementalNexus] Комбинация '" + combo.getId()
                    + "' зарегистрирована повторно — прежняя версия заменена.");
        }
        combos.put(combo.getId(), combo);
    }

    public Combo getCombo(String id) {
        return combos.get(id);
    }

    public Set<String> getComboIds() {
        return Collections.unmodifiableSet(combos.keySet());
    }

    /**
     * Основная точка входа для использования комбинации игроком.
     * Стихии уже распознаны командой /magic combine; порядок не важен.
     */
    public void combine(Player player, Element first, Element second) {
        NexusConfig config = plugin.getNexusConfig();

        if (first == second) {
            player.sendMessage(config.msg("same-elements"));
            return;
        }

        String key = Combo.keyOf(first, second);
        Combo combo = combos.get(key);
        if (combo == null) {
            player.sendMessage(config.msg("combination-not-found",
                    "first", first.getRussianName(),
                    "second", second.getRussianName()));
            return;
        }

        AbilitySettings settings = config.getComboSettings(key);
        if (settings == null) {
            player.sendMessage(config.msg("combination-not-found",
                    "first", first.getRussianName(),
                    "second", second.getRussianName()));
            return;
        }

        long remainingSeconds = getRemainingCooldownSeconds(player, key);
        if (remainingSeconds > 0) {
            player.sendMessage(config.msg("spell-on-cooldown",
                    "spell", settings.getDisplayName(),
                    "time", String.valueOf(remainingSeconds)));
            return;
        }

        double manaCost = settings.getManaCost();
        if (!plugin.getManaManager().hasMana(player, manaCost)) {
            player.sendMessage(config.msg("not-enough-mana",
                    "cost", FormatUtil.num(manaCost),
                    "current", FormatUtil.num(plugin.getManaManager().getMana(player))));
            return;
        }

        // Событие вызывается в основном потоке: команды выполняются в нём.
        ElementCombinationEvent event = new ElementCombinationEvent(player, first, second, key);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage(config.msg("spell-cancelled"));
            return;
        }

        // Отменённое PlayerManaChangeEvent тоже отменяет комбинацию.
        boolean spent = plugin.getManaManager().removeMana(player, manaCost, ManaChangeReason.COMBINATION_USE);
        if (!spent) {
            player.sendMessage(config.msg("spell-cancelled"));
            return;
        }

        setCooldown(player, key, settings.getCooldownSeconds());

        try {
            combo.performCast(player, settings);
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ElementalNexus] Ошибка при использовании комбинации '" + key + "'", throwable);
            player.sendMessage(config.msg("cast-error"));
        }
    }

    public long getRemainingCooldownSeconds(Player player, String id) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }
        Long expiry = playerCooldowns.get(id);
        if (expiry == null) {
            return 0;
        }
        long remainingMillis = expiry - System.currentTimeMillis();
        return remainingMillis <= 0 ? 0 : (remainingMillis + 999) / 1000;
    }

    private void setCooldown(Player player, String id, double cooldownSeconds) {
        long expiry = System.currentTimeMillis() + (long) (cooldownSeconds * 1000.0);
        cooldowns.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>()).put(id, expiry);
    }
}

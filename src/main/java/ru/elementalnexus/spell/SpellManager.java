package ru.elementalnexus.spell;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.config.NexusConfig;
import ru.elementalnexus.event.SpellCastEvent;
import ru.elementalnexus.mana.ManaChangeReason;
import ru.elementalnexus.spell.impl.FireballSpell;
import ru.elementalnexus.spell.impl.LightningStrikeSpell;
import ru.elementalnexus.spell.impl.WaterWaveSpell;
import ru.elementalnexus.spell.impl.WindGustSpell;
import ru.elementalnexus.util.FormatUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Реестр обычных заклинаний и логика их использования:
 * проверка перезарядки и маны, вызов SpellCastEvent, списание маны.
 */
public class SpellManager {

    private final ElementalNexusPlugin plugin;

    private final Map<String, Spell> spells = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public SpellManager(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует заклинания четырёх стихий.
     */
    public void registerDefaults() {
        register(new FireballSpell(plugin));
        register(new WindGustSpell(plugin));
        register(new WaterWaveSpell(plugin));
        register(new LightningStrikeSpell(plugin));
    }

    public void register(Spell spell) {
        String id = spell.getId().toLowerCase(Locale.ROOT);
        if (spells.containsKey(id)) {
            plugin.getLogger().warning("[ElementalNexus] Заклинание '" + id
                    + "' зарегистрировано повторно — прежняя версия заменена.");
        }
        spells.put(id, spell);
    }

    public Spell getSpell(String id) {
        return spells.get(id.toLowerCase(Locale.ROOT));
    }

    public Set<String> getSpellIds() {
        return Collections.unmodifiableSet(spells.keySet());
    }

    /**
     * Основная точка входа для использования заклинания игроком.
     */
    public void cast(Player player, String rawId) {
        NexusConfig config = plugin.getNexusConfig();
        String id = rawId.toLowerCase(Locale.ROOT);

        Spell spell = spells.get(id);
        if (spell == null) {
            player.sendMessage(config.msg("unknown-spell", "spell", rawId));
            return;
        }

        AbilitySettings settings = config.getSpellSettings(id);
        if (settings == null) {
            player.sendMessage(config.msg("unknown-spell", "spell", rawId));
            return;
        }

        long remainingSeconds = getRemainingCooldownSeconds(player, id);
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
        SpellCastEvent event = new SpellCastEvent(player, id, manaCost);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage(config.msg("spell-cancelled"));
            return;
        }

        // Отменённое PlayerManaChangeEvent тоже отменяет заклинание:
        // мана не списана — перезарядка и эффект не запускаются.
        boolean spent = plugin.getManaManager().removeMana(player, manaCost, ManaChangeReason.SPELL_USE);
        if (!spent) {
            player.sendMessage(config.msg("spell-cancelled"));
            return;
        }

        setCooldown(player, id, settings.getCooldownSeconds());

        try {
            spell.performCast(player, settings);
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ElementalNexus] Ошибка при использовании заклинания '" + id + "'", throwable);
            player.sendMessage(config.msg("cast-error"));
        }
    }

    /**
     * @return оставшееся время перезарядки в секундах (округление вверх)
     */
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

package ru.elementalnexus.spell.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.spell.Spell;
import ru.elementalnexus.util.TargetUtil;

/**
 * LIGHTNING: удар молнии по цели. Используется визуальный эффект
 * молнии (без реального удара — не создаёт пожар и не разрушает
 * окружение), а урон наносится вручную.
 */
public class LightningStrikeSpell extends Spell {

    public LightningStrikeSpell(ElementalNexusPlugin plugin) {
        super(plugin, "lightning_strike", Element.LIGHTNING);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double range = settings.getDouble("range", 30.0);
        double damage = settings.getDouble("damage", 10.0);

        LivingEntity target = TargetUtil.targetEntity(caster, range);
        Location point = TargetUtil.targetPoint(caster, range);

        // Только визуальный эффект: огня и разрушений нет.
        caster.getWorld().strikeLightningEffect(point);
        caster.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point.clone().add(0, 1, 0),
                20, 0.3, 0.5, 0.3, 0.05);

        if (target != null) {
            target.damage(damage, caster);
        }
    }
}

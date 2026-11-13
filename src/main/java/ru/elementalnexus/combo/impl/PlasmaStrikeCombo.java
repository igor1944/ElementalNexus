package ru.elementalnexus.combo.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.Combo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.util.TargetUtil;

/**
 * FIRE + LIGHTNING: плазменный удар по цели — визуальная молния,
 * повышенный урон и огненные искры.
 */
public class PlasmaStrikeCombo extends Combo {

    public PlasmaStrikeCombo(ElementalNexusPlugin plugin) {
        super(plugin, Element.FIRE, Element.LIGHTNING);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double range = settings.getDouble("range", 30.0);
        double damage = settings.getDouble("damage", 14.0);

        LivingEntity target = TargetUtil.targetEntity(caster, range);
        Location point = TargetUtil.targetPoint(caster, range);

        caster.getWorld().strikeLightningEffect(point);
        caster.getWorld().spawnParticle(Particle.FLAME, point.clone().add(0, 0.5, 0),
                30, 0.3, 0.5, 0.3, 0.05);
        caster.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point.clone().add(0, 0.5, 0),
                20, 0.3, 0.5, 0.3, 0.08);

        if (target != null) {
            target.damage(damage, caster);
        }
    }
}

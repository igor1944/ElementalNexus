package ru.elementalnexus.combo.impl;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.Combo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.util.TargetUtil;

import java.util.List;

/**
 * WATER + LIGHTNING: электрический разряд, поражающий несколько
 * ближайших целей визуальной молнией и уроном.
 */
public class ChainDischargeCombo extends Combo {

    public ChainDischargeCombo(ElementalNexusPlugin plugin) {
        super(plugin, Element.WATER, Element.LIGHTNING);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 10.0);
        double damage = settings.getDouble("damage", 6.0);
        int maxTargets = (int) settings.getDouble("max-targets", 4.0);

        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        int affected = 0;
        for (LivingEntity target : targets) {
            if (affected >= maxTargets) {
                break;
            }
            affected++;
            // Визуальная молния без пожара и разрушений + ручной урон.
            caster.getWorld().strikeLightningEffect(target.getLocation());
            caster.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
            target.damage(damage, caster);
        }

        if (affected == 0) {
            // Нет целей — просто вспышка вокруг заклинателя.
            caster.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    caster.getLocation().add(0, 1, 0), 40, 2.0, 1.0, 2.0, 0.05);
        }
    }
}

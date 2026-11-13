package ru.elementalnexus.combo.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.Combo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.util.TargetUtil;

import java.util.List;

/**
 * WIND + LIGHTNING: грозовой порыв — мощный порыв ветра с громом,
 * небольшим уроном и сильным отбрасыванием.
 */
public class StormGustCombo extends Combo {

    public StormGustCombo(ElementalNexusPlugin plugin) {
        super(plugin, Element.WIND, Element.LIGHTNING);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 8.0);
        double power = settings.getDouble("knockback-power", 2.0);
        double damage = settings.getDouble("damage", 3.0);

        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        for (LivingEntity target : targets) {
            if (damage > 0) {
                target.damage(damage, caster);
            }
            Vector push = target.getLocation().toVector()
                    .subtract(caster.getLocation().toVector());
            push.setY(0);
            if (push.lengthSquared() < 0.0001) {
                push = caster.getLocation().getDirection();
                push.setY(0);
            }
            push.normalize().multiply(power);
            push.setY(0.35 * power);
            target.setVelocity(push);
        }

        caster.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, caster.getLocation().add(0, 1.2, 0),
                30, radius * 0.5, 1.0, radius * 0.5, 0.08);
        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation().add(0, 1, 0),
                30, radius * 0.5, 1.0, radius * 0.5, 0.03);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }
}

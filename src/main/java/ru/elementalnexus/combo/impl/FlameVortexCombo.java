package ru.elementalnexus.combo.impl;

import org.bukkit.Location;
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
 * FIRE + WIND: огненный вихрь — урон по области вокруг заклинателя,
 * вихрь слегка затягивает цели к центру.
 */
public class FlameVortexCombo extends Combo {

    public FlameVortexCombo(ElementalNexusPlugin plugin) {
        super(plugin, Element.FIRE, Element.WIND);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 6.0);
        double damage = settings.getDouble("damage", 8.0);

        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        for (LivingEntity target : targets) {
            target.damage(damage, caster);
            Vector pull = caster.getLocation().toVector()
                    .subtract(target.getLocation().toVector());
            pull.setY(0);
            if (pull.lengthSquared() > 0.0001) {
                pull.normalize().multiply(0.6);
                pull.setY(0.4);
                target.setVelocity(pull);
            }
        }

        // Спираль из огненных частиц вокруг заклинателя.
        double particleRadius = Math.min(radius, 6.0) * 0.8;
        Location center = caster.getLocation();
        for (int ring = 0; ring < 3; ring++) {
            double y = 0.3 + ring * 0.8;
            double ringRadius = particleRadius * (1.0 - ring * 0.2);
            for (int i = 0; i < 12; i++) {
                double angle = (Math.PI * 2 / 12) * i + ring;
                Location point = center.clone().add(
                        Math.cos(angle) * ringRadius, y, Math.sin(angle) * ringRadius);
                caster.getWorld().spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0);
            }
        }
        caster.getWorld().playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.6f);
    }
}

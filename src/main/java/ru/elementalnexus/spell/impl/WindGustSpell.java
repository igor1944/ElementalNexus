package ru.elementalnexus.spell.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.spell.Spell;
import ru.elementalnexus.util.TargetUtil;

import java.util.List;

/**
 * WIND: порыв ветра, отбрасывающий ближайшие сущности от заклинателя.
 */
public class WindGustSpell extends Spell {

    public WindGustSpell(ElementalNexusPlugin plugin) {
        super(plugin, "wind_gust", Element.WIND);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 6.0);
        double power = settings.getDouble("knockback-power", 1.4);

        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        for (LivingEntity target : targets) {
            Vector push = target.getLocation().toVector()
                    .subtract(caster.getLocation().toVector());
            push.setY(0);
            if (push.lengthSquared() < 0.0001) {
                push = caster.getLocation().getDirection();
                push.setY(0);
            }
            push.normalize().multiply(power);
            push.setY(0.4 * power);
            target.setVelocity(push);
        }

        caster.getWorld().spawnParticle(Particle.CLOUD,
                caster.getLocation().add(0, 1, 0),
                40, radius * 0.5, 1.0, radius * 0.5, 0.02);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.2f);
    }
}

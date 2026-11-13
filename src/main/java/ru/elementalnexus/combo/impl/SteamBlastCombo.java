package ru.elementalnexus.combo.impl;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.Combo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.util.TargetUtil;

import java.util.List;

/**
 * FIRE + WATER: паровой взрыв — визуальный взрыв без разрушения блоков,
 * отбрасывание и эффект слабости на ближайших врагов.
 */
public class SteamBlastCombo extends Combo {

    public SteamBlastCombo(ElementalNexusPlugin plugin) {
        super(plugin, Element.FIRE, Element.WATER);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 5.0);
        double power = settings.getDouble("knockback-power", 1.2);
        double weaknessDuration = settings.getDouble("weakness-duration-seconds", 5.0);
        double weaknessAmplifier = settings.getDouble("weakness-amplifier", 0.0);

        // Взрыв с нулевой мощностью: только визуальный эффект и звук,
        // блоки не разрушаются, огонь не создаётся.
        caster.getWorld().createExplosion(caster.getLocation(), 0f, false, false);

        int durationTicks = (int) (weaknessDuration * 20.0);
        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        for (LivingEntity target : targets) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                    durationTicks, (int) weaknessAmplifier));
            Vector push = target.getLocation().toVector()
                    .subtract(caster.getLocation().toVector());
            push.setY(0);
            if (push.lengthSquared() < 0.0001) {
                push = caster.getLocation().getDirection();
                push.setY(0);
            }
            push.normalize().multiply(power);
            push.setY(0.45 * power);
            target.setVelocity(push);
        }

        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation().add(0, 1, 0),
                30, radius * 0.5, 0.8, radius * 0.5, 0.03);
        caster.getWorld().spawnParticle(Particle.SMOKE_NORMAL, caster.getLocation().add(0, 1, 0),
                20, radius * 0.4, 0.6, radius * 0.4, 0.02);
    }
}

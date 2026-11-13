package ru.elementalnexus.spell.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.spell.Spell;
import ru.elementalnexus.util.TargetUtil;

import java.util.List;

/**
 * WATER: водяная волна, замедляющая и слегка отбрасывающая сущности
 * вокруг заклинателя.
 */
public class WaterWaveSpell extends Spell {

    /**
     * Тип эффекта замедления. На Paper 1.18.2 поле называется SLOW,
     * в новых версиях — SLOWNESS, поэтому тип определяется по ключу
     * "slowness", который одинаков во всех версиях.
     */
    private static final PotionEffectType SLOWNESS_TYPE = resolveSlowness();

    public WaterWaveSpell(ElementalNexusPlugin plugin) {
        super(plugin, "water_wave", Element.WATER);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 6.0);
        double durationSeconds = settings.getDouble("slowness-duration-seconds", 4.0);
        double slownessAmplifier = settings.getDouble("slowness-amplifier", 1.0);
        double power = settings.getDouble("knockback-power", 0.8);

        int durationTicks = (int) (durationSeconds * 20.0);
        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        for (LivingEntity target : targets) {
            if (SLOWNESS_TYPE != null) {
                target.addPotionEffect(new PotionEffect(SLOWNESS_TYPE, durationTicks, (int) slownessAmplifier));
            }
            Vector push = target.getLocation().toVector()
                    .subtract(caster.getLocation().toVector());
            push.setY(0);
            if (push.lengthSquared() < 0.0001) {
                push = caster.getLocation().getDirection();
                push.setY(0);
            }
            push.normalize().multiply(power);
            push.setY(0.25 * power);
            target.setVelocity(target.getVelocity().add(push));
        }

        caster.getWorld().spawnParticle(Particle.WATER_SPLASH,
                caster.getLocation().add(0, 1, 0),
                60, radius * 0.5, 0.5, radius * 0.5, 0.1);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.8f);
    }

    private static PotionEffectType resolveSlowness() {
        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft("slowness"));
        if (type == null) {
            type = PotionEffectType.getByName("SLOW");
        }
        return type;
    }
}

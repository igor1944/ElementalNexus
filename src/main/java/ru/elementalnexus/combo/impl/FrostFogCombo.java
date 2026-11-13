package ru.elementalnexus.combo.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.combo.Combo;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.util.TargetUtil;

import java.util.List;

/**
 * WIND + WATER: ледяной туман — густой туман, замедляющий цели
 * в радиусе вокруг заклинателя.
 */
public class FrostFogCombo extends Combo {

    /**
     * Тип эффекта замедления определяется по ключу "slowness",
     * чтобы работать и на 1.18.2 (поле SLOW), и на новых версиях.
     */
    private static final PotionEffectType SLOWNESS_TYPE = resolveSlowness();

    public FrostFogCombo(ElementalNexusPlugin plugin) {
        super(plugin, Element.WIND, Element.WATER);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double radius = settings.getDouble("radius", 7.0);
        double durationSeconds = settings.getDouble("slowness-duration-seconds", 6.0);
        double slownessAmplifier = settings.getDouble("slowness-amplifier", 1.0);

        int durationTicks = (int) (durationSeconds * 20.0);
        List<LivingEntity> targets = TargetUtil.livingAround(caster, radius);
        for (LivingEntity target : targets) {
            if (SLOWNESS_TYPE != null) {
                target.addPotionEffect(new PotionEffect(SLOWNESS_TYPE, durationTicks, (int) slownessAmplifier));
            }
        }

        caster.getWorld().spawnParticle(Particle.SNOWFLAKE, caster.getLocation().add(0, 1, 0),
                60, radius * 0.5, 0.8, radius * 0.5, 0.03);
        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation().add(0, 0.5, 0),
                40, radius * 0.5, 0.5, radius * 0.5, 0.01);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.6f, 0.5f);
    }

    private static PotionEffectType resolveSlowness() {
        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft("slowness"));
        if (type == null) {
            type = PotionEffectType.getByName("SLOW");
        }
        return type;
    }
}

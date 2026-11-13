package ru.elementalnexus.spell.impl;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;
import ru.elementalnexus.spell.Spell;

/**
 * FIRE: огненный снаряд. Запускает маленький файербол, который не
 * разрушает блоки и не поджигает территорию. Урон наносится через
 * ProjectileListener по метаданным снаряда.
 */
public class FireballSpell extends Spell {

    public FireballSpell(ElementalNexusPlugin plugin) {
        super(plugin, "fireball", Element.FIRE);
    }

    @Override
    public void performCast(Player caster, AbilitySettings settings) {
        double damage = settings.getDouble("damage", 8.0);
        double speed = settings.getDouble("speed", 1.5);

        Vector direction = caster.getEyeLocation().getDirection().normalize().multiply(speed);
        SmallFireball fireball = caster.launchProjectile(SmallFireball.class, direction);
        fireball.setIsIncendiary(false); // не поджигает блоки
        fireball.setYield(0f);           // не разрушает блоки взрывом
        fireball.getPersistentDataContainer()
                .set(plugin.getFireballDamageKey(), PersistentDataType.DOUBLE, damage);

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
        caster.getWorld().spawnParticle(Particle.FLAME,
                caster.getEyeLocation().add(caster.getEyeLocation().getDirection()),
                10, 0.1, 0.1, 0.1, 0.02);
    }
}

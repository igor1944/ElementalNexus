package ru.elementalnexus.listener;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import ru.elementalnexus.ElementalNexusPlugin;

/**
 * Обработка снаряда заклинания «Огненный снаряд»:
 * переопределяет урон по метаданным снаряда и показывает
 * эффект при попадании в блок.
 */
public class ProjectileListener implements Listener {

    private final ElementalNexusPlugin plugin;

    public ProjectileListener(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }
        Projectile projectile = (Projectile) event.getDamager();
        Double damage = projectile.getPersistentDataContainer()
                .get(plugin.getFireballDamageKey(), PersistentDataType.DOUBLE);
        if (damage != null) {
            // Заменяем стандартный урон снаряда на настроенный в конфигурации.
            event.setDamage(damage);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!projectile.getPersistentDataContainer()
                .has(plugin.getFireballDamageKey(), PersistentDataType.DOUBLE)) {
            return;
        }
        if (event.getHitBlock() != null) {
            Location location = event.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
            projectile.getWorld().spawnParticle(Particle.FLAME, location, 12, 0.2, 0.2, 0.2, 0.03);
        }
    }
}

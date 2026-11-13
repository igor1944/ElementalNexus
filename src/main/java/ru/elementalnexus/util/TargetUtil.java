package ru.elementalnexus.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Помощники поиска целей для заклинаний.
 */
public final class TargetUtil {

    private TargetUtil() {
    }

    /**
     * Трассировка взгляда игрока: ищет и блоки, и живые сущности.
     *
     * @return результат трассировки или null, если на дистанции ничего нет
     */
    public static RayTraceResult rayTrace(Player player, double range) {
        Location eye = player.getEyeLocation();
        return player.getWorld().rayTrace(eye, eye.getDirection(), range,
                FluidCollisionMode.NEVER, true, 0.6,
                entity -> !entity.equals(player) && entity instanceof LivingEntity);
    }

    /**
     * Пойманная взглядом живая сущность или null.
     */
    public static LivingEntity targetEntity(Player player, double range) {
        RayTraceResult result = rayTrace(player, range);
        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            return (LivingEntity) result.getHitEntity();
        }
        return null;
    }

    /**
     * Точка попадания взгляда (сущность или блок). Если на дистанции
     * ничего нет — точка на максимальной дальности.
     */
    public static Location targetPoint(Player player, double range) {
        RayTraceResult result = rayTrace(player, range);
        if (result != null) {
            return result.getHitPosition().toLocation(player.getWorld());
        }
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();
        return eye.add(direction.multiply(range));
    }

    /**
     * Все живые сущности в радиусе вокруг источника (кроме самого источника),
     * отсортированные по расстоянию (ближние — первыми).
     */
    public static List<LivingEntity> livingAround(Entity source, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        Location center = source.getLocation();
        double radiusSquared = radius * radius;
        for (Entity entity : source.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living
                    && !entity.equals(source)
                    && entity.isValid()
                    && entity.getLocation().distanceSquared(center) <= radiusSquared) {
                result.add(living);
            }
        }
        result.sort(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(center)));
        return result;
    }
}

package ru.elementalnexus.combo;

import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;

import java.util.Locale;

/**
 * Базовый класс комбинированного заклинания из двух разных стихий.
 * Порядок стихий не важен: ключ комбинации канонизируется.
 */
public abstract class Combo {

    protected final ElementalNexusPlugin plugin;

    private final Element first;
    private final Element second;
    private final String id;

    protected Combo(ElementalNexusPlugin plugin, Element first, Element second) {
        this.plugin = plugin;
        this.first = first;
        this.second = second;
        this.id = keyOf(first, second);
    }

    /**
     * Канонический ключ пары стихий, не зависящий от порядка:
     * fire + wind и wind + fire дают один и тот же ключ "fire_wind".
     */
    public static String keyOf(Element a, Element b) {
        if (a.ordinal() <= b.ordinal()) {
            return a.getId().toLowerCase(Locale.ROOT) + "_" + b.getId().toLowerCase(Locale.ROOT);
        }
        return b.getId().toLowerCase(Locale.ROOT) + "_" + a.getId().toLowerCase(Locale.ROOT);
    }

    public String getId() {
        return id;
    }

    public Element getFirst() {
        return first;
    }

    public Element getSecond() {
        return second;
    }

    /**
     * Применяет эффект комбинации. Мана уже списана, перезарядка
     * уже запущена, событие ElementCombinationEvent уже прошло проверку.
     */
    public abstract void performCast(Player caster, AbilitySettings settings);
}

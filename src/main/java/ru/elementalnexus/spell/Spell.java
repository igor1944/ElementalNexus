package ru.elementalnexus.spell;

import org.bukkit.entity.Player;
import ru.elementalnexus.ElementalNexusPlugin;
import ru.elementalnexus.config.AbilitySettings;
import ru.elementalnexus.element.Element;

/**
 * Базовый класс обычного заклинания.
 */
public abstract class Spell {

    protected final ElementalNexusPlugin plugin;

    private final String id;
    private final Element element;

    protected Spell(ElementalNexusPlugin plugin, String id, Element element) {
        this.plugin = plugin;
        this.id = id;
        this.element = element;
    }

    public String getId() {
        return id;
    }

    public Element getElement() {
        return element;
    }

    /**
     * Применяет эффект заклинания. Мана уже списана, перезарядка
     * уже запущена, событие SpellCastEvent уже прошло проверку.
     */
    public abstract void performCast(Player caster, AbilitySettings settings);
}

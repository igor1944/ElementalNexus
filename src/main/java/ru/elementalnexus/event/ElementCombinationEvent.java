package ru.elementalnexus.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.elementalnexus.element.Element;

/**
 * Событие попытки использования комбинации двух стихий.
 *
 * Содержит игрока, обе стихии и ID комбинированного заклинания.
 * Событие отменяемое: при отмене мана не тратится, перезарядка
 * не запускается и эффект комбинации не применяется.
 * Вызывается только в основном потоке сервера.
 */
public class ElementCombinationEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private final Player player;
    private final Element firstElement;
    private final Element secondElement;
    private final String combinationId;

    public ElementCombinationEvent(Player player, Element firstElement, Element secondElement, String combinationId) {
        this.player = player;
        this.firstElement = firstElement;
        this.secondElement = secondElement;
        this.combinationId = combinationId;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @return первая стихия в том порядке, в каком её указал игрок
     */
    public Element getFirstElement() {
        return firstElement;
    }

    /**
     * @return вторая стихия в том порядке, в каком её указал игрок
     */
    public Element getSecondElement() {
        return secondElement;
    }

    /**
     * @return канонический ID комбинации, например "fire_wind"
     * (порядок стихий в ID не зависит от порядка ввода)
     */
    public String getCombinationId() {
        return combinationId;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

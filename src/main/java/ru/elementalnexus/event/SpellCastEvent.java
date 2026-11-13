package ru.elementalnexus.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие попытки использования заклинания.
 *
 * Содержит игрока, ID заклинания и стоимость маны.
 * Событие отменяемое: при отмене мана не тратится, перезарядка
 * не запускается и эффект заклинания не применяется.
 * Вызывается только в основном потоке сервера.
 */
public class SpellCastEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private final Player player;
    private final String spellId;
    private final double manaCost;

    public SpellCastEvent(Player player, String spellId, double manaCost) {
        this.player = player;
        this.spellId = spellId;
        this.manaCost = manaCost;
    }

    public Player getPlayer() {
        return player;
    }

    public String getSpellId() {
        return spellId;
    }

    public double getManaCost() {
        return manaCost;
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

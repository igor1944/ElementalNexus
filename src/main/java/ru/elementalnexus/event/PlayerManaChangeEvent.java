package ru.elementalnexus.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import ru.elementalnexus.mana.ManaChangeReason;

/**
 * Событие изменения маны игрока.
 *
 * Содержит игрока, старое и новое количество маны и причину изменения.
 * Событие отменяемое: при отмене мана игрока не изменяется.
 * Вызывается только в основном потоке сервера.
 */
public class PlayerManaChangeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private final double oldMana;
    private double newMana;
    private final ManaChangeReason reason;

    public PlayerManaChangeEvent(Player player, double oldMana, double newMana, ManaChangeReason reason) {
        super(player);
        this.oldMana = oldMana;
        this.newMana = newMana;
        this.reason = reason;
    }

    /**
     * @return количество маны до изменения
     */
    public double getOldMana() {
        return oldMana;
    }

    /**
     * @return количество маны после изменения
     */
    public double getNewMana() {
        return newMana;
    }

    /**
     * Позволяет обработчикам скорректировать новое значение маны.
     * Итоговое значение всё равно будет ограничено диапазоном [0, максимум].
     */
    public void setNewMana(double newMana) {
        this.newMana = newMana;
    }

    /**
     * @return причина изменения маны
     */
    public ManaChangeReason getReason() {
        return reason;
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

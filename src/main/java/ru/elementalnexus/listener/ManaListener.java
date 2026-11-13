package ru.elementalnexus.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.elementalnexus.ElementalNexusPlugin;

/**
 * Сохранение и загрузка маны при входе/выходе игрока.
 */
public class ManaListener implements Listener {

    private final ElementalNexusPlugin plugin;

    public ManaListener(ElementalNexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getManaManager().loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getManaManager().unloadPlayer(event.getPlayer());
    }
}

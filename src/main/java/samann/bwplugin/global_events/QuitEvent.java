package samann.bwplugin.global_events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuitEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        event.setQuitMessage("§3" + event.getPlayer().getDisplayName() + "§7 hat den Server verlassen.");
    }
}

package samann.bwplugin.global_events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if(message.contains("fuck")){
            player.sendMessage("§cDu verwendest unangemessene Wörter!");
            //block chat massage
            event.setCancelled(true);
            return;
        }

        event.setFormat("§3" + player.getDisplayName() + "§7: §f" + message);

    }
}

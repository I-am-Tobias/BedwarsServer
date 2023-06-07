package samann.bwplugin.global_events;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import samann.bwplugin.BwPlugin;

public class JoinEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        joiner.setDisplayName("§3" + joiner.getDisplayName());
        event.setJoinMessage(joiner.getDisplayName() + "§7 hat den Server betreten.");
        joiner.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);

        BwPlugin.playerRole.addEntry(joiner.getName());

        BwPlugin.sendToMainLobby(joiner);
    }

}

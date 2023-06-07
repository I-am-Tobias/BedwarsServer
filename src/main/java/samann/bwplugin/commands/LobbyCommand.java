package samann.bwplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;

public class LobbyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //teleports sender to lobby
        Player player = (Player) sender;
        BwPlugin.sendToMainLobby(player);
        return true;

    }
}

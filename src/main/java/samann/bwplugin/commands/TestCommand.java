package samann.bwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.Game;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) return false;

        World world = ((Player) sender).getWorld();
        Game currentGame = BwPlugin.getGame(world);


        return true;
    }
}

package samann.bwplugin.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaySound implements CommandExecutor {
    int i = 0;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) i = Integer.parseInt(args[0]);

        Sound sound = Sound.values()[i];
        Player player = (Player) sender;
        player.playSound(player.getLocation(), sound, 1, 1);
        player.sendMessage("Playing sound " + i + ": " + sound.name());
        i++;
        return true;
    }
}

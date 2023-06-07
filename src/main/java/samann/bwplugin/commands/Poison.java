package samann.bwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class Poison implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) return false;

        //gives the sender poison effect for 30 seconds
        Player player = (Player) sender;
        player.addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.POISON, 30 * 20, 0));

        return true;
    }

}

package samann.bwplugin.commands;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import samann.bwplugin.BwPlugin;

public class SummonShulker implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) return false;

        Player player = (Player) sender;
        World world = player.getWorld();
        Location loc = player.getLocation();
        loc = BwPlugin.getIntegerLocation(loc);

        Shulker shulker = (Shulker) world.spawnEntity(loc, EntityType.SHULKER);
        shulker.setAI(false);
        shulker.setCollidable(false);
        shulker.setSilent(true);
        shulker.setGravity(false);
        shulker.setRemoveWhenFarAway(false);
        shulker.setCanPickupItems(false);
        shulker.setPersistent(true);
        shulker.setInvulnerable(false);
        shulker.setColor(DyeColor.WHITE);

        if(args.length > 0){
            shulker.setCustomName(args[0].replace("&", "§").replace("§§", "&").replace("_", " "));
            shulker.setCustomNameVisible(true);
        }

        return true;
    }
}

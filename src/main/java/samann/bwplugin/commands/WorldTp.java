package samann.bwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldTp implements CommandExecutor, TabCompleter {
    Argument<World> worldArg = new Argument<World>("world", (String s) -> {
        World w = Bukkit.getWorld(s);
        if(w != null) return w;
        return new WorldCreator(s).createWorld();
    }, () -> {
        List<String> list = new ArrayList<String>();
        for(World w : Bukkit.getWorlds()){
            list.add(w.getName());
        }
        File[] files = Bukkit.getWorldContainer().listFiles();
        for (File file : files) {
            //if directory contains level.dat, it is a world
            if(new File(file.getAbsolutePath() + "/level.dat").exists()){
                if(!list.contains(file.getName())) list.add(file.getName());
            }
        }

        return list;
    });

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) return false;

        //teleports sender to world with name args[0]
        Player player = (Player) sender;
        World world = worldArg.parse(args[0], sender);
        assert world != null;
        player.teleport(BwPlugin.getIntegerLocation(world.getSpawnLocation()));
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return worldArg.autocompletion(args[0], sender);
    }
}

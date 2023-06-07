package samann.bwplugin.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateWorld implements CommandExecutor, TabCompleter {
    Argument<Material> blockArg = new Argument<Material>("material", (String str) -> {
        Material block = null;
        try{
            block = Material.valueOf(str.toUpperCase());
            if(block.isBlock()) return block;
            return null;
        }catch(IllegalArgumentException e){
            return null;
        }
    }, () -> {
        List<String> list = new ArrayList<String>();
        for(Material m : Material.values()){
            if(m.isBlock()) list.add(m.name());
        }
        return list;
    });

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!sender.isOp()) return false;
        if(args.length == 0){
            sender.sendMessage("/createworld <name> <layers...>");
        }else{
            String name = args[0];
            if(Bukkit.getWorld(name) != null){
                sender.sendMessage("World already exists");
                return true;
            }

            if(args.length == 1){
                new WorldCreator(name).createWorld();
            }else{
                WorldCreator worldCreator = new WorldCreator(name);
                worldCreator.generateStructures(false);
                worldCreator.environment(World.Environment.NORMAL);
                worldCreator.type(WorldType.FLAT);
                //doesn't work for some reason
                String generatorSettings = "{\"structures\": {\"structures\": {}}, \"layers\": [";
                for(int i = 1; i < args.length; i++){
                    generatorSettings += "{\"block\": \"" + blockArg.parse(args[i], sender).name().toLowerCase() + "\", \"height\": 1}";
                    if(i != args.length - 1){
                        generatorSettings += ",";
                    }
                }
                generatorSettings += "], \"biome\":\"plains\"}";
                worldCreator.generatorSettings(generatorSettings);
                System.out.println(generatorSettings);
                worldCreator.createWorld();
            }
            sender.sendMessage("world successfully created");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 1){
            return Collections.singletonList("<name>");
        }else{
            return blockArg.autocompletion(args[args.length - 1], sender);
        }
    }
}

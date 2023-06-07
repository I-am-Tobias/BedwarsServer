package samann.bwplugin.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MobTower implements CommandExecutor, TabCompleter {
    public Argument<EntityType> mobArgument = new Argument<EntityType>("mob", (String str) -> {
        EntityType mob = null;
        try{
            mob = EntityType.valueOf(str.toUpperCase());
            if(mob.isSpawnable() && mob.isAlive()) return mob;
            return null;
        }catch(IllegalArgumentException e){
            return null;
        }
    }, () -> {
        List<String> list = new ArrayList<String>();
        for(EntityType e : EntityType.values()){
            if(e.isAlive() && e.isSpawnable()) list.add(e.name().toUpperCase());
        }
        return list;
    });


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!sender.isOp()) return false;

        List<EntityType> mobList = new ArrayList<EntityType>();
        for (String arg : args) {
            if (mobArgument.isValid(arg, sender)) {
                EntityType mob = mobArgument.parse(arg, sender);
                mobList.add(mob);
            }
        }

        Location loc = ((Player) sender).getLocation();

        Entity lastMob = null;
        while(mobList.size() > 0){
            if(lastMob == null){
                lastMob = Objects.requireNonNull(loc.getWorld()).spawnEntity(loc, mobList.get(0));
            }else{
                Entity mob = Objects.requireNonNull(loc.getWorld()).spawnEntity(loc, mobList.get(0));
                lastMob.addPassenger(mob);
                lastMob = mob;
            }
            mobList.remove(0);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return mobArgument.autocompletion(args[args.length - 1], sender);
    }
}

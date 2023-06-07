package samann.bwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.bedwars.Bedwars;
import samann.bwplugin.bedwars.MapData;
import samann.bwplugin.bedwars.Rules;
import samann.bwplugin.bedwars.TeamColor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BedwarsCommand implements CommandExecutor, TabCompleter {
    //commands:
    // /bw setspawn <team>
    // /bw setteam <team> <player>
    // /bw start
    // /bw init
    // /bw stop
    // /bw spawner
    // /bw toggleSmash
    private static final List<String> commands = Arrays.asList("setspawn", "setteam", "autoteam", "init", "start", "stop", "spawner", "save", "load", "toggleSmash");
    public Argument<String> commandArg = new Argument<>("command", s -> commands.contains(s) ? s : null, () -> {
        return new ArrayList<>(commands);
    });
    public Argument<TeamColor> teamArg = new Argument<TeamColor>("team", s -> TeamColor.valueOf(s.toUpperCase()), () -> {
        List<String> list = new ArrayList<String>();
        for(TeamColor c : TeamColor.values()){
            list.add(c.name().toUpperCase());
        }
        return list;
    });
    public Argument<Player> playerArg = new Argument<Player>("player", (String s, CommandSender cs) -> {
            List<Player> players = ((Player)cs).getWorld().getPlayers();
            for(Player p : players){
                if(p.getName().equals(s)){
                    return p;
                }
            }
            return null;
        }, (CommandSender cs) -> {
            List<String> list = new ArrayList<String>();
            for(Player p : ((Player)cs).getWorld().getPlayers()){
                list.add(p.getName());
            }
            return list;
        });
    public Argument<String> mapArg = new Argument<String>("map", (String s, CommandSender cs) -> {
            //if valid world name, return it
            File world = new File(Bukkit.getWorldContainer(), s);
            if(world.exists() && world.isDirectory()){
                if(new File(world, "bedwars.mapData").exists()){
                    return s;
                }
            }
            return "";
        }, (CommandSender cs) -> {
            //get all directories in the world folder
            File worldContainer = Bukkit.getWorldContainer();
            File[] worldDirs = worldContainer.listFiles();
            List<String> list = new ArrayList<String>();
            for(File f : worldDirs){
                if(f.isDirectory()){
                    if(new File(f, "bedwars.mapData").exists()){
                        list.add(f.getName());
                    }
                }
            }
            return list;
        });

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) return false;
        Bedwars bedwarsGame = null;
        try {
            bedwarsGame = (Bedwars) BwPlugin.getGame(((Player)sender).getWorld());
        }catch (ClassCastException ignored){}


        if(args.length == 0){
            sender.sendMessage("/bw <command>");
            return false;
        }
        String arg0 = commandArg.parse(args[0], sender);

        World world = null;
        if(sender instanceof Player){
            world = ((Player)sender).getWorld();
        }else if(sender instanceof BlockCommandSender){
            world = ((BlockCommandSender)sender).getBlock().getWorld();
        }

        switch(arg0.toLowerCase()){
            case "setspawn":
                if(args.length < 2){
                    sender.sendMessage("/bw setspawn <team>");
                    return false;
                }else{
                    TeamColor team = teamArg.parse(args[1], sender);
                    if(team == null) return false;
                    Location loc = ((Player)sender).getLocation();
                    loc = BwPlugin.getIntegerLocation(loc);
                    bedwarsGame.teamData(team).setSpawn(loc);
                    sender.sendMessage("Set spawn for team " + team.name().toUpperCase() + " to " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                    return true;
                }
            case "setteam":
                if(args.length < 2){
                    sender.sendMessage("/bw setteam <team> <player>");
                    return false;
                }else{
                    TeamColor team = teamArg.parse(args[1], sender);
                    Player player;
                    if(args.length < 3) player = (Player)sender;
                    else player = playerArg.parse(args[2], sender);

                    if(player == null || team == null) return false;

                    bedwarsGame.assignTeam(player, team);
                    sender.sendMessage("Assigned " + player.getName() + " to team " + team.colorCode() + team.name().toUpperCase());
                    return true;
                }
            case "autoteam":
                bedwarsGame.autoTeam(world.getPlayers());
                sender.sendMessage("Assigned all players to teams");
                return true;
            case "init":
                new Bedwars(world);
                sender.sendMessage("Initialized Bedwars");
                return true;
            case "start":
                String args1 = "";
                if(args.length > 1){
                    args1 = mapArg.parse(args[1], sender);
                }

                if(args1.equals("")){
                    bedwarsGame.start();
                    sender.sendMessage("Started game");
                }else{
                    try {
                        bedwarsGame = loadMap(args1);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        return true;
                    }
                    sender.sendMessage("Loaded map " + args1);
                    //Bedwars.instance.autoTeam(world.getPlayers());
                    //new ArrayList<>(Bukkit.getServer().getOnlinePlayers())
                    bedwarsGame.autoTeam(world.getPlayers());
                    bedwarsGame.start();
                    sender.sendMessage("Started game in world " + args1);
                }
                return true;
            case "stop":
                if(bedwarsGame == null) return true;
                bedwarsGame.end();
                sender.sendMessage("Stopped game");
                return true;
            case "spawner":
                if(bedwarsGame == null) return false;
                boolean addedSpawner = bedwarsGame.toggleSpawner(BwPlugin.getIntegerLocation(((Player)sender).getLocation()));
                if(addedSpawner) sender.sendMessage("Added spawner");
                else sender.sendMessage("Removed spawner");
                return true;
            case "save":
                //saves Bedwars.mapData to file using serialization
                if(bedwarsGame == null) return false;
                try {
                    MapData mapData = bedwarsGame.mapData;
                    world.save();
                    FileOutputStream fos = new FileOutputStream(world.getName() + "/bedwars.mapData");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(mapData);
                    oos.close();
                    fos.close();
                    sender.sendMessage("Saved bedwars.mapData");
                } catch (IOException e) {
                    sender.sendMessage("Error saving Bedwars.mapData: " + e.getMessage());
                }
                return true;
            case "load":
                try {
                    String name = world.getName();
                    FileInputStream fis = new FileInputStream(name + "/bedwars.mapData");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    MapData mapData = (MapData)ois.readObject();
                    ois.close();
                    fis.close();
                    new Bedwars(mapData, world);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }
                sender.sendMessage("Loaded standardMap/bedwars.mapData");
                return true;
            case "togglesmash":
                Rules r = Rules.staticRules;
                r.smashBw = !r.smashBw;
                sender.sendMessage("Smash Bedwars is now " + (r.smashBw ? "enabled" : "disabled"));
                return true;
            default:
                return false;
        }
    }
    private Bedwars loadMap(String name) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(name + "/bedwars.mapData");
        ObjectInputStream ois = new ObjectInputStream(fis);
        MapData mapData = (MapData)ois.readObject();
        ois.close();
        fis.close();
        return mapData.initGame(name);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> defaultList = new ArrayList<String>();

        if(args.length == 1) {
            return commandArg.autocompletion(args[0], sender);
        }else if(args.length == 2) {
            switch(args[0]) {
                case "setspawn":
                case "setteam":
                    return teamArg.autocompletion(args[1], sender);
                case "start":
                    return mapArg.autocompletion(args[1], sender);
                default:
                    return defaultList;
            }
        }else if(args.length == 3){
            switch(args[0]){
                case "setteam":
                    //returns list with all players in world
                    return playerArg.autocompletion(args[2], sender);
                default:
                    return defaultList;
            }
        }else return defaultList;
    }



}

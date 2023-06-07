package samann.bwplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.bedwars.stats.BedwarsStats;
import samann.bwplugin.commands.*;
import samann.bwplugin.games.Game;
import samann.bwplugin.global_events.*;
import samann.bwplugin.lobby.Lobby;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class BwPlugin extends JavaPlugin {
    public static BwPlugin instance;
    public static Team playerRole;
    public final static List<Game> games = new ArrayList<>();

    @Override
    public void onEnable() {
        enableCommands();
        registerEvents();

        if(instance == null) instance = this;

        BedwarsStats.loadStats();

        initializeTeams();


        new Lobby(Bukkit.getWorld("lobby"));
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            sendToMainLobby(p);
        }



        getLogger().info("BwPlugin is enabled!");
    }

    @Override
    public void onDisable() {
        while(!games.isEmpty()) {
            games.get(0).end();
        }

        BedwarsStats.saveStats();

        getLogger().info("BwPlugin is disabled!");
    }

    void initializeTeams(){
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        scoreboard.getTeams().forEach(Team::unregister);

        playerRole = scoreboard.getTeam("playerRole");
        if(playerRole == null) playerRole = scoreboard.registerNewTeam("playerRole");
        playerRole.setColor(ChatColor.DARK_AQUA);
        playerRole.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    public static void sendToMainLobby(Player player){
        player.teleport(Bukkit.getWorld("lobby").getSpawnLocation());

        resetAllAttributes(player);
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        System.out.println("stml: " + player.getAllowFlight());
    }

    public static void resetAllAttributes(Player player){
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)).setBaseValue(0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.1);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(2);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR)).setBaseValue(0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)).setBaseValue(0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(4);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_LUCK)).setBaseValue(0);
    }

    @Nullable
    public static Game getGame(World world){
        for(Game game : games){
            if(game.world.equals(world)) return game;
        }
        return null;
    }



    void registerEvents(){
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new JoinEvent(), this);
        pm.registerEvents(new QuitEvent(), this);
        pm.registerEvents(new ChatEvent(), this);
    }


    void enableCommands(){
        getCommand("test").setExecutor(new TestCommand());
        getCommand("poison").setExecutor(new Poison());
        getCommand("summonshop").setExecutor(new SummonShop());
        BedwarsCommand bwCmd = new BedwarsCommand();
        getCommand("bedwars").setExecutor(bwCmd);
        getCommand("bedwars").setTabCompleter(bwCmd);
        getCommand("playsound").setExecutor(new PlaySound());
        getCommand("lobby").setExecutor(new LobbyCommand());
        getCommand("worldtp").setExecutor(new WorldTp());
        getCommand("summonshulker").setExecutor(new SummonShulker());
        MobTower mobTower = new MobTower();
        getCommand("mobtower").setExecutor(mobTower);
        getCommand("mobtower").setTabCompleter(mobTower);
        CreateWorld createWorld = new CreateWorld();
        getCommand("createworld").setExecutor(createWorld);
        getCommand("createworld").setTabCompleter(createWorld);
        StatsCommand statsCommand = new StatsCommand();
        getCommand("stats").setExecutor(statsCommand);
        getCommand("stats").setTabCompleter(statsCommand);
    }

    //takes a location and returns a location with integer coordinates
    //also changes the direction to a multiple of 45 degrees (round)
    public static Location getIntegerLocation(Location loc){
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();
        float newYaw = (float)Math.round(yaw/45)*45;
        float newPitch = (float)Math.round(pitch/45)*45;
        return new Location(loc.getWorld(), x + 0.5, y, z + 0.5, newYaw, newPitch);
    }
}

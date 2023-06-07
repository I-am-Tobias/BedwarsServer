package samann.bwplugin.games;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.events.GameEvent;
import samann.bwplugin.games.events.GameEventManager;

import java.util.ArrayList;
import java.util.List;

public abstract class Game {
    public final World world;
    protected final String prefix;
    protected boolean running = false;
    protected final List<BukkitTask> tasks = new ArrayList<>();
    protected final List<GamePlayer> players = new ArrayList<>();
    protected final GameEventManager eventManager;


    public Game(World world, String prefix) {
        this.world = world;
        this.prefix = prefix;
        this.eventManager = new GameEventManager(this);

        BwPlugin.games.add(this);

        registerEvents();
        initializeWorld();

        world.getPlayers().forEach(this::onPlayerJoinWorld);
    }
    protected abstract void initializeWorld();

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (running) {
            return;
        }
        runRepeatedTask(new BukkitRunnable() {
            @Override
            public void run() {
                try{
                    onTick();
                }catch(Exception e){
                    BwPlugin.instance.getLogger().info(e.getMessage());
                }
            }
        }, 0, 1);

        players.forEach(GamePlayer::onStart);
        running = true;
    }
    public void end() {
        running = false;

        while(players.size() > 0) {
            removePlayer(players.get(0));
        }
        for (BukkitTask task : tasks) {
            if(!task.isCancelled()) task.cancel();
        }
        tasks.clear();
        if(world.getPlayers().size() > 0) {
            //send remaining players to lobby
            world.getPlayers().forEach(BwPlugin::sendToMainLobby);
        }
        BwPlugin.games.remove(this);
        Bukkit.unloadWorld(world, false);
        //WorldCopier.deleteFolder(world.getWorldFolder().getPath());
    }
    protected abstract void onTick();

    public BukkitTask runRepeatedTask(BukkitRunnable task, long delay, long period) {
        BukkitTask t = task.runTaskTimer(BwPlugin.instance, delay, period);
        addTask(t);
        return t;
    }
    public void addTask(BukkitTask task) {
        tasks.add(task);
    }


    public void msgAll(String msg, boolean prefix){
        world.getPlayers().forEach(p -> p.sendMessage(prefix ? this.prefix + msg : msg));
    }
    public void msgAll(String msg){
        msgAll(msg, false);
    }

    public GamePlayer getPlayer(Player player){
        for(GamePlayer p : players){
            if(p.player.equals(player)){
                return p;
            }
        }
        return null;
    }
    public <T extends GamePlayer> List<T> getPlayers(Class<T> type){
        List<T> list = new ArrayList<>();
        for(GamePlayer p : players){
            if(type.isInstance(p)){
                list.add(type.cast(p));
            }
        }
        return list;
    }
    public void addPlayer(GamePlayer player){
        if(getPlayer(player.player) != null){
            removePlayer(getPlayer(player.player));
        }
        for(Game g : new ArrayList<>(BwPlugin.games)){
            if(g.getPlayer(player.player) != null){
                g.removePlayer(g.getPlayer(player.player));
            }
        }
        players.add(player);
        if(running){
            player.onStart();
        }
    }
    public void removePlayer(@Nullable GamePlayer player){
        players.remove(player);
        if(player != null){
            player.onEnd();
        }
    }
    public void onPlayerLeaveWorld(Player player){
        GamePlayer mgp = getPlayer(player);
        if(mgp == null) return;
        removePlayer(mgp);
        if(players.size() == 0 && !running){
            end();
        }
    }
    public void onPlayerQuit(Player player){
        GamePlayer mgp = getPlayer(player);
        if(mgp == null) return;

        players.remove(mgp);
        if(players.size() == 0 && !running){
            end();
        }
    }
    public void onPlayerJoinWorld(Player player){
        Spectator spectator = new Spectator(player, this);
        addPlayer(spectator);
    }
    public void registerEvents(){
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(PlayerJoinEvent event) {
                if(ignoreEvent(event) && getPlayer(event.getPlayer()) == null) return;

                onPlayerJoinWorld(event.getPlayer());
            }
        });
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(PlayerQuitEvent event) {
                if(ignoreEvent(event) && getPlayer(event.getPlayer()) == null) return;

                onPlayerQuit(event.getPlayer());
            }
        });
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(PlayerChangedWorldEvent event) {
                if(ignoreEvent(event)) return;

                if(event.getFrom().equals(event.getPlayer().getWorld())) return;

                if(event.getPlayer().getWorld().equals(world)) onPlayerJoinWorld(event.getPlayer());
                else onPlayerLeaveWorld(event.getPlayer());
            }
        });

    }
}

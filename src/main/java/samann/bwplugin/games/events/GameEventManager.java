package samann.bwplugin.games.events;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.Game;

import java.util.ArrayList;
import java.util.List;

public class GameEventManager {
    public final Game game;
    public final List<GameEvent> events = new ArrayList<>();

    public GameEventManager(Game game) {
        this.game = game;
    }

    public void register(GameEvent event) {
        event.game = game;
        events.add(event);
        Bukkit.getPluginManager().registerEvents(event, BwPlugin.instance);
    }
    public void unregister(GameEvent event) {
        events.remove(event);
        HandlerList.unregisterAll(event);
    }
}

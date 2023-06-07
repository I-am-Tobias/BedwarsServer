package samann.bwplugin.games;

import org.bukkit.entity.Player;

public abstract class GamePlayer {
    public final Player player;
    public final Game game;


    public GamePlayer(Player player, Game game) {
        this.player = player;
        this.game = game;
    }

    public abstract void onStart();
    public abstract void onEnd();

    public boolean isSpectator() {
        return this instanceof Spectator;
    }

    public void msg(String msg, boolean prefix){
        if(!player.isOnline()) return;
        player.sendMessage(prefix ? game.prefix + msg : msg);
    }
    public void msg(String msg){
        msg(msg, false);
    }
}

package samann.bwplugin.games;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;

import java.util.Objects;

public class Spectator extends GamePlayer {
    public Spectator(Player player, Game game) {
        super(player, game);
    }
    public void onStart() {
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(100);
        player.getInventory().clear();
        player.setCollidable(false);
        player.setInvisible(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setGameMode(GameMode.SPECTATOR);
        player.setCanPickupItems(false);
        player.setInvulnerable(true);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setLevel(0);
        player.teleport(game.world.getSpawnLocation());
    }
    public void onEnd() {
        player.setFlying(false);
        player.setCollidable(true);
        player.setInvisible(false);
        player.setAllowFlight(false);
        player.setCanPickupItems(true);
        player.setInvulnerable(false);

        BwPlugin.resetAllAttributes(player);
    }

}

package samann.bwplugin.lobby;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.Game;
import samann.bwplugin.games.GamePlayer;

import java.util.Objects;

public class LobbyPlayer extends GamePlayer {

    public LobbyPlayer(Player player, Game game) {
        super(player, game);
    }

    @Override
    public void onStart() {
        player.teleport(game.world.getSpawnLocation());
        player.setDisplayName("§3" + player.getName());
        player.setPlayerListName("§3" + player.getName());
        player.getInventory().clear();
        player.setCollidable(true);
        player.setInvisible(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setFlying(false);
        player.setAllowFlight(true);
        player.setCanPickupItems(true);
        player.setInvulnerable(false);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setExp(0);
        player.setLevel(0);
        player.setFreezeTicks(0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(100);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.setAbsorptionAmount(0);
    }

    @Override
    public void onEnd() {
        BwPlugin.resetAllAttributes(player);
    }
}

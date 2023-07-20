package samann.bwplugin.airwars;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import samann.bwplugin.bedwars.Bedwars;
import samann.bwplugin.games.events.GameEvent;
import samann.bwplugin.pvp.ComboPvp;

public class AirwarsEvents extends GameEvent {
  Airwars airwarsGame;
  ComboPvp pvp;

  public AirwarsEvents(Airwars game) {
    airwarsGame = game;
    pvp = new ComboPvp(game);
  }

  @EventHandler
  public void onEvent(BlockBreakEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onEvent(BlockPlaceEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onEvent(EntityPickupItemEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onEvent(EntityDamageEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onEvent(EntityDamageByEntityEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
    pvp.hit(event);
  }

  @EventHandler
  public void onEvent(PlayerToggleFlightEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
    AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer(event.getPlayer());
    if (player.boostAvailable) {
      player.boostAvailable = false;
      event.getPlayer().setAllowFlight(false);

      // boost
      Vector direction = event.getPlayer().getLocation().getDirection().clone().normalize();
      direction.setY(Math.abs(direction.getY()));

      final double gravity = 32d / (20 * 20);
      final double minBoostHeight = 10d;
      final double maxBoostHeight = 10d;
      final double maxBoostDistance = 20;

      double minVerticalSpeed = Math.sqrt(2 * gravity * minBoostHeight);
      double maxVerticalSpeed = Math.sqrt(2 * gravity * maxBoostHeight);
      double verticalSpeed = minVerticalSpeed + direction.getY() * (maxVerticalSpeed - minVerticalSpeed);

      double time = 2 * verticalSpeed / gravity;
      double maxHorizontalSpeed = maxBoostDistance / time;

      Vector velocity = new Vector(direction.getX() * maxHorizontalSpeed, verticalSpeed, direction.getZ() * maxHorizontalSpeed);
      player.player.setVelocity(velocity);
    }
  }

  @EventHandler
  public void onEvent(EntityRegainHealthEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
  }
}

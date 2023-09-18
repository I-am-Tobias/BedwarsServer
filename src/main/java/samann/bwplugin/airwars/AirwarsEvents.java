package samann.bwplugin.airwars;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;
import samann.bwplugin.airwars.items.Crossbow;
import samann.bwplugin.airwars.items.FishingRod;
import samann.bwplugin.games.events.GameEvent;

public class AirwarsEvents extends GameEvent {
  Airwars airwarsGame;
  AirwarsPvp pvp;

  public AirwarsEvents(Airwars game) {
    airwarsGame = game;
    pvp = new AirwarsPvp(game);
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
  public void onEvent(PlayerDropItemEvent event) {
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
    pvp.hitEvent(event);
  }

  @EventHandler
  public void onEvent(PlayerToggleFlightEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);
    AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer(event.getPlayer());
    if (player.getBoostAvailable()) {
      player.setBoostAvailable(false);

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

  @EventHandler
  public void onEvent(PlayerInteractEvent event) {
    if (ignoreEvent(event)) return;
    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer(event.getPlayer());
      for (var item : player.items) {
        if (item.isItem(event.getItem())) {
          if (item.callUseOnRightClick()) {
            item.use();
            event.setCancelled(true);
          } else if (item.hasCooldown()) {
            event.setCancelled(true);
          }
          return;
        }
      }
    }
  }

  @EventHandler
  public void onEvent(ProjectileLaunchEvent event) {
    if (ignoreEvent(event)) return;

    AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer((Player) event.getEntity().getShooter());
    if (player == null) return;

    if (event.getEntity() instanceof Trident) {
      event.setCancelled(true);
      var tridentItem = player.getItem(samann.bwplugin.airwars.items.Trident.class);
      if (tridentItem != null) {
        tridentItem.use();
      }
    } else if (event.getEntity() instanceof FishHook hook) {
      var fishingRod = player.getItem(FishingRod.class);
      if (fishingRod != null) {
        fishingRod.onThrowHook(hook);
      }
    } else if (event.getEntity() instanceof Arrow arrow) {
      var crossbow = player.getItem(Crossbow.class);
      if (crossbow != null) {
        crossbow.reset();
      }
    }
  }

  @EventHandler
  public void onEvent(ProjectileHitEvent event) {
    if (ignoreEvent(event)) return;

    AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer((Player) event.getEntity().getShooter());
    if (player == null) return;

    if (event.getEntity() instanceof Fireball fireball) {
      if (event.getHitEntity() == fireball.getShooter()) {
        event.setCancelled(true);
      }
    } else if (event.getEntity() instanceof Arrow arrow) {
      event.setCancelled(true);
      airwarsGame.world.playSound(arrow.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);
      if (event.getHitEntity() instanceof Player targetPlayer
              && airwarsGame.getPlayer(targetPlayer) instanceof AirwarsPlayer target) {
        var crossbow = player.getItem(Crossbow.class);
        if (crossbow != null) {
          crossbow.onHit();
        }
        var direction = arrow.getVelocity();
        AirwarsPvp.hit(player, target, 2, direction);
        player.player.playSound(player.player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        arrow.remove();
      }
      if (event.getHitEntity() == null) {
        arrow.remove();
      }
    }
  }

  @EventHandler
  public void onEvent(ExplosionPrimeEvent event) {
    if (ignoreEvent(event)) return;
    event.setCancelled(true);

    var shooter = (AirwarsPlayer) airwarsGame.getPlayer((Player) ((Fireball) event.getEntity()).getShooter());

    var center = event.getEntity().getLocation();
    var radius = event.getRadius();
    var entities = airwarsGame.world.getNearbyEntities(
            center,
            radius + 1,
            radius + 1,
            radius + 1,
            e -> e != shooter.player && e instanceof Player player && airwarsGame.getPlayer(player) instanceof AirwarsPlayer
    );

    for (var entity : entities) {
      var player = (AirwarsPlayer) airwarsGame.getPlayer((Player) entity);
      double distance = entity.getBoundingBox().getCenter().distance(center.toVector());
      if (distance > radius) continue;

      double strength = Math.sqrt(radius - distance);
      var direction = (entity.getBoundingBox().getCenter().subtract(center.toVector())).normalize();

      AirwarsPvp.hit(shooter, player, strength, direction);
    }

    airwarsGame.world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 10f, 1f);
    airwarsGame.world.spawnParticle(Particle.EXPLOSION_LARGE, center, (int) (radius * radius), radius / 4, radius / 4, radius / 4);
  }
}

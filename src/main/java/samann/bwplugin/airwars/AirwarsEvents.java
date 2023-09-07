package samann.bwplugin.airwars;

import net.minecraft.world.entity.projectile.FishingHook;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
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
    pvp.hit(event);
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
    if (event.getEntity() instanceof Trident trident) {
      event.setCancelled(true);
      AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer((Player) trident.getShooter());
      for (var item : player.items) {
        if (item instanceof samann.bwplugin.airwars.items.Trident) {
          item.use();
        }
      }
    } else if (event.getEntity() instanceof FishHook hook) {
      AirwarsPlayer player = (AirwarsPlayer) airwarsGame.getPlayer((Player) hook.getShooter());
      for (var item : player.items) {
        if (item instanceof FishingRod) {
          ((FishingRod) item).onThrowHook(hook);
        }
      }
    }
  }

  @EventHandler
  public void onEvent(ProjectileHitEvent event) {
    if (ignoreEvent(event)) return;

    if (event.getEntity() instanceof Fireball fireball) {
      if (event.getHitEntity() == fireball.getShooter()) {
        event.setCancelled(true);
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
            e -> e instanceof Player player && airwarsGame.getPlayer(player) instanceof AirwarsPlayer
    );

    for (var entity : entities) {
      var player = (AirwarsPlayer) airwarsGame.getPlayer((Player) entity);
      double distance = entity.getBoundingBox().getCenter().distance(center.toVector());
      if (distance > radius) continue;

      double strength = Math.sqrt(radius - distance) * player.knockbackMultiplier;
      var direction = (entity.getBoundingBox().getCenter().subtract(center.toVector())).normalize();
      var boost = direction.multiply(strength);
      boost.setY(boost.getY() / 4);

      player.player.setVelocity(player.player.getVelocity().multiply(0.5).add(direction.multiply(strength)));
      player.hitBy(shooter);
      player.knockbackMultiplier += Math.sqrt(radius - distance) * 0.2;
    }

    airwarsGame.world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 10f, 1f);
    airwarsGame.world.spawnParticle(Particle.EXPLOSION_LARGE, center, (int) (radius * radius), radius / 4, radius / 4, radius / 4);
  }
}

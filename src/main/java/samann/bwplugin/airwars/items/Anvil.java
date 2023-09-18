package samann.bwplugin.airwars.items;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.AirwarsPlayer;
import samann.bwplugin.airwars.AirwarsPvp;

public class Anvil extends Item {
  private boolean isActive = false;
  private final Runnable tick = this::tick;

  public Anvil(AirwarsPlayer player) {
    super(player, Material.ANVIL, ChatColor.GRAY, "Stampfattacke",
            "Aktiviere dieses Item, während du in der Luft bist, um dich fallen zu lassen und Gegner im Umkreis wegzustoßen.",
            10 * 20);
  }

  @Override
  protected void useItem() {
    isActive = true;
    setEnchanted(true);
    player.addItemTick(tick);
    player.player.setVelocity(new Vector(0, -2, 0));
  }

  @Override
  protected boolean canUse() {
    return !isActive && !player.player.isOnGround();
  }

  private void tick() {
    if (player.player.isOnGround()) {
      var world = player.game.world;
      double radius = 5;
      var entities = world.getNearbyEntities(player.player.getLocation(), radius, 1.5, radius);
      for (var entity : entities) {
        if (entity.getLocation().distance(player.player.getLocation()) > radius) continue;
        if (!(entity instanceof Player t && player.game.getPlayer(t) instanceof AirwarsPlayer target)) continue;
        if (target == player) continue;
        var direction = target.player.getLocation().toVector()
                .subtract(player.player.getLocation().toVector());
        float strength = 1.5f;
        AirwarsPvp.hit(player, target, strength, direction);
      }
      //world.playSound(player.player.getLocation(), Sound.BLOCK_BELL_USE, 10, 0.8f);
      world.playEffect(player.player.getLocation(), Effect.ANVIL_LAND, 0);
      world.spawnParticle(Particle.SPIT, player.player.getLocation(), 30, radius / 2, 0, radius / 2);
      reset();
    } else if (player.player.getVelocity().getY() > 0) {
      reset();
      removeCooldown();
    }
  }

  @Override
  public void reset() {
    super.reset();
    player.removeItemTick(tick);
    isActive = false;
    setEnchanted(false);
  }

  @Override
  public Progress currentProgress() {
    if (isActive) {
      return new Progress(Progress.State.ACTIVE, 1);
    } else {
      return super.currentProgress();
    }
  }
}

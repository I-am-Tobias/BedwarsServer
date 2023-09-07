package samann.bwplugin.airwars.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import samann.bwplugin.airwars.AirwarsPlayer;
import samann.bwplugin.airwars.AirwarsPvp;

public class Anvil extends Item {
  private static final ItemStack ITEM = new ItemStack(Material.FLINT_AND_STEEL);
  private boolean isActive = false;
  private final Runnable tick = this::tick;

  static {
    var meta = ITEM.getItemMeta();
    meta.setDisplayName("STAMPFATTACKE");
    ITEM.setItemMeta(meta);
  }

  public Anvil(AirwarsPlayer player) {
    super(ITEM, 10 * 20, player);
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
                .subtract(player.player.getLocation().toVector())
                .setY(0)
                .normalize();
        float strength = 1.5f;
        strength *= target.knockbackMultiplier;
        AirwarsPvp.takeKnockback(target.player, strength, direction.getX(), direction.getZ());
        target.hitBy(player);
        target.player.playHurtAnimation(0);
      }
      world.playSound(player.player.getLocation(), Sound.BLOCK_BELL_USE, 10, 0.8f);
      world.spawnParticle(Particle.SPIT, player.player.getLocation(), 30, radius / 2, 0, radius / 2);
      reset();
    }
    if (player.player.getVelocity().getY() > 0) {
      reset();
    }
  }

  @Override
  public void reset() {
    super.reset();
    player.removeItemTick(tick);
    isActive = false;
    setEnchanted(false);
  }
}
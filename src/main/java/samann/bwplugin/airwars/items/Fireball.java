package samann.bwplugin.airwars.items;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import samann.bwplugin.airwars.AirwarsPlayer;

public class Fireball extends Item {
  private static final ItemStack ITEM = new ItemStack(Material.MAGMA_CREAM);
  private static final double SPEED = 10;

  static {
    var meta = ITEM.getItemMeta();
    meta.setDisplayName("Feuerball");
    ITEM.setItemMeta(meta);
  }

  public Fireball(AirwarsPlayer player) {
    super(ITEM, 20 * 20, player);
  }

  @Override
  protected void useItem() {
    var world = player.game.world;
    var fireball = (org.bukkit.entity.Fireball)
            world.spawnEntity(player.player.getEyeLocation(), EntityType.FIREBALL);
    fireball.setDirection(player.player.getLocation().getDirection());
    fireball.setVelocity(fireball.getDirection().multiply(SPEED));
    fireball.setYield(5);
    fireball.setShooter(player.player);

    reset();
  }

  @Override
  protected boolean canUse() {
    return true;
  }
}

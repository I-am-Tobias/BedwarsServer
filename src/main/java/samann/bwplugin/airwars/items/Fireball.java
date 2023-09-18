package samann.bwplugin.airwars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.AirwarsPlayer;

public class Fireball extends Item {
  private static final double SPEED = 10;

  public Fireball(AirwarsPlayer player) {
    super(player, Material.MAGMA_CREAM, ChatColor.GOLD, "Feuerball",
            "Wirf einen Feuerball, der beim Auftreffen eine Explosion auslöst.",
            20 * 20);//20
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

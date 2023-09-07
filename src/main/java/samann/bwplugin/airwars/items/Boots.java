package samann.bwplugin.airwars.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import samann.bwplugin.airwars.AirwarsPlayer;

public class Boots extends Item {
  private static final ItemStack ITEM = new ItemStack(Material.GOLDEN_BOOTS);

  static {
    var meta = ITEM.getItemMeta();
    meta.setDisplayName("Doppelsprung zurücksetzen");
    ITEM.setItemMeta(meta);
  }

  public Boots(AirwarsPlayer player) {
    super(ITEM, 15 * 20, player);
  }

  @Override
  protected void useItem() {
    player.setBoostAvailable(true);
    reset();
  }

  @Override
  protected boolean canUse() {
    return !player.getBoostAvailable();
  }
}

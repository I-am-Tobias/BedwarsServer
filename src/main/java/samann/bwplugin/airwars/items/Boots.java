package samann.bwplugin.airwars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.AirwarsPlayer;

public class Boots extends Item {
  public Boots(AirwarsPlayer player) {
    super(player, Material.GOLDEN_BOOTS, ChatColor.YELLOW, "Erneuter Sprung",
            "Aktiviere dieses Item, während du in der Luft bist und keinen Doppelsprung mehr hast, um einen zusätzlichen Doppelsprung zu erhalten.",
            15 * 20);
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

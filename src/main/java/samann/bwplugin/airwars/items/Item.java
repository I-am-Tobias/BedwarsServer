package samann.bwplugin.airwars.items;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import samann.bwplugin.airwars.AirwarsPlayer;

public abstract class Item {
  public final ItemStack item;
  public final int cooldown;
  public final AirwarsPlayer player;

  public Item(ItemStack item, int cooldown, AirwarsPlayer player) {
    assert item.getType().isItem() && item.getItemMeta() != null;
    this.item = item.clone();
    var meta = item.getItemMeta();
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
    this.cooldown = cooldown;
    this.player = player;

    player.player.getInventory().addItem(this.item);
  }

  public void use() {
    if (hasCooldown() || !canUse()) {
      return;
    }

    useItem();
  }

  public void reset() {
    player.player.setCooldown(item.getType(), cooldown);
  }

  protected abstract void useItem();

  protected boolean canUse() {
    return true;
  }

  protected void setEnchanted(boolean enchanted) {
    var inventory = player.player.getInventory();
    for (int i = 0; i < inventory.getSize(); i++) {
      var item = inventory.getItem(i);
      if (item == null) continue;
      if (isItem(item)) {
        if (enchanted) {
          item.addEnchantment(Enchantment.DURABILITY, 1);
        } else {
          item.removeEnchantment(Enchantment.DURABILITY);
        }
      }
    }
  }

  protected boolean isEnchanted() {
    var inventory = player.player.getInventory();
    for (int i = 0; i < inventory.getSize(); i++) {
      var item = inventory.getItem(i);
      if (item == null) continue;
      if (isItem(item)) {
        return item.getItemMeta().hasEnchant(Enchantment.DURABILITY);
      }
    }
    return false;
  }

  protected void setDurability(double d) {
    var inventory = player.player.getInventory();
    for (int i = 0; i < inventory.getSize(); i++) {
      var item = inventory.getItem(i);
      if (item == null) continue;
      if (isItem(item)) {
        int max = item.getType().getMaxDurability();
        Damageable dmg = (Damageable) item.getItemMeta();
        dmg.setDamage((int) ((1 - d) * max));
        item.setItemMeta(dmg);
      }
    }
  }

  public boolean isItem(ItemStack other) {
    if (other == null) return false;
    return item.getType() == other.getType();
  }

  public boolean callUseOnRightClick() {
    return true;
  }

  public boolean hasCooldown() {
    return player.player.hasCooldown(item.getType());
  }
}

package samann.bwplugin.airwars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.AirwarsPlayer;

import java.util.List;

public abstract class Item {
  public final ItemStack item;
  public final int cooldown;
  public final AirwarsPlayer player;

  public Item(AirwarsPlayer player, ItemStack item, int cooldown) {
    this.item = item.clone();
    this.player = player;
    this.cooldown = cooldown;
    if (player != null) {
      player.player.getInventory().addItem(this.item);
    }
  }

  public Item(AirwarsPlayer player, Material material, ChatColor color, String name, String description, int cooldown) {
    this(player, createItem(material, color, name, description, cooldown), cooldown);
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
    var item = getInventoryItem();
    if (item != null) {
      if (enchanted) {
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
      } else {
        item.removeEnchantment(Enchantment.DURABILITY);
      }
    }
  }

  protected boolean isEnchanted() {
    var item = getInventoryItem();
    return item != null && item.getItemMeta().hasEnchant(Enchantment.DURABILITY);
  }

  protected void setDurability(double d) {
    var item = getInventoryItem();
    if (item != null) {
      int max = item.getType().getMaxDurability();
      Damageable dmg = (Damageable) item.getItemMeta();
      dmg.setDamage((int) ((1 - d) * max));
      item.setItemMeta(dmg);
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

  protected void removeCooldown() {
    player.player.setCooldown(item.getType(), 0);
  }

  @Nullable
  protected ItemStack getInventoryItem() {
    var inventory = player.player.getInventory();
    for (int i = 0; i < inventory.getSize(); i++) {
      var item = inventory.getItem(i);
      if (isItem(item)) return item;
    }
    return null;
  }

  protected static ItemStack createItem(Material material, ChatColor color, String name, String description, int cooldown) {
    var item = new ItemStack(material);
    var meta = item.getItemMeta();
    meta.addItemFlags(ItemFlag.values());
    var cooldownString = "" + (cooldown % 20 == 0 ? (cooldown / 20) : (cooldown / 20 + "," + (cooldown % 20) / 2));
    meta.setDisplayName(color + "§l" + name + "  §8⌚ " + cooldownString + "s");
    meta.setLore(List.of("", "§7" + description));
    item.setItemMeta(meta);
    return item;
  }
}

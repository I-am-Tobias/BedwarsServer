package samann.bwplugin.bedwars.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import samann.bwplugin.bedwars.shop.Category;
import samann.bwplugin.bedwars.shop.ShopInventory;
import samann.bwplugin.games.events.GameEvent;

import java.util.Objects;

public class ShopEvents extends GameEvent {
    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        if(ignoreEvent(event)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryView view = player.getOpenInventory();


        if(view.getTitle().equals("Shop") && Objects.equals(view.getInventory(event.getRawSlot()), view.getTopInventory())) {
            event.setCancelled(true);
            boolean isValidClick = event.isLeftClick() || event.isRightClick() || event.getClick().equals(ClickType.NUMBER_KEY);
            if(event.getClick().equals(ClickType.DOUBLE_CLICK)) isValidClick = false;
            if(isValidClick){
                if (event.getSlot() < Category.values().length) {
                    player.openInventory(new ShopInventory(player, Category.values()[event.getSlot()]));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                } else {
                    ItemStack item = event.getCurrentItem();
                    if(item == null || item.getType() == Material.AIR) return;

                    int price = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(0).split(" ")[1]);
                    if (player.getLevel() >= price) {
                        //remove price from item
                        ItemStack newItem = item.clone();

                        ItemMeta meta = newItem.getItemMeta();
                        assert meta != null;
                        meta.setLore(null);

                        //if item is block, reset meta
                        if (meta.getLocalizedName().startsWith("block")) {
                            meta = Bukkit.getItemFactory().getItemMeta(newItem.getType());
                        }
                        newItem.setItemMeta(meta);


                        int amount = 1;
                        if (event.isShiftClick()) amount = player.getLevel() / price;
                        if (amount * item.getAmount() > item.getMaxStackSize())
                            amount = item.getMaxStackSize() / item.getAmount();
                        if (amount * price > player.getLevel()) amount = player.getLevel() / price;

                        player.setLevel(player.getLevel() - amount * price);
                        newItem.setAmount(amount * newItem.getAmount());

                        Inventory inv = player.getInventory();
                        int slot = event.getHotbarButton();
                        if(slot != -1){
                            ItemStack old = inv.getItem(slot);
                            inv.setItem(slot, newItem);
                            if(old != null) inv.addItem(old);
                        }else{
                            inv.addItem(newItem);
                        }

                        player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, 0.5f, 2);
                    } else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1);
                }
            }
        }else if(view.getTitle().equals("Shop") && event.isShiftClick()){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onDragItem(InventoryDragEvent event) {
        if(ignoreEvent(event)) return;

        InventoryView view = event.getWhoClicked().getOpenInventory();

        if(view.getTitle().equals("Shop")) {
            event.setCancelled(true);
        }
    }
}

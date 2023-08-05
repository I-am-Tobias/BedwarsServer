package samann.bwplugin.bedwars.shop;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShopInventory extends InventoryView {
    private Inventory topInventory;
    private Inventory bottomInventory;
    private HumanEntity player;
    private InventoryType type;
    private String title;
    private Category selectedCategory;

    public ShopInventory(Player player, Category selectedCategory) {
        this.player = player;
        this.type = InventoryType.CHEST;
        title = "Shop";
        bottomInventory = player.getInventory();
        topInventory = Bukkit.createInventory(player, 9*2, "Shop");
        this.selectedCategory = selectedCategory;


        List<ItemStack> categories = getCategories();
        List<ItemStack> items = getItems();

        for(int i = 0; i < categories.size(); i++) {
            topInventory.setItem(i, categories.get(i));
        }
        for(int i = 0; i < items.size(); i++) {
            topInventory.setItem(i+9, items.get(i));
        }
    }

    @Override
    public Inventory getTopInventory() {
        return topInventory;
    }

    @Override
    public Inventory getBottomInventory() {
        return bottomInventory;
    }

    @Override
    public HumanEntity getPlayer() {
        return player;
    }

    @Override
    public InventoryType getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @NotNull
    @Override
    public String getOriginalTitle() {
        return title;
    }

    @Override
    public void setTitle(@NotNull String title) {
        throw new RuntimeException("not allowed :(");
    }


    private List<ItemStack> getCategories() {
        List<ItemStack> categories = new ArrayList<>();
        for (Category category : Category.values()) {
            ItemStack item = category.getItemStack(category.equals(selectedCategory));
            categories.add(item);
        }
        return categories;
    }

    private List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : selectedCategory.getItems()) {
            items.add(item);
        }
        return items;
    }
}

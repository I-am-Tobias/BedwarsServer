package samann.bwplugin.bedwars.shop;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public enum Category {
    BLOCKS,
    TOOLS,
    WEAPONS,
    ARMOR,
    FOOD,
    POTIONS,
    SPECIAL;

    public ItemStack getItemStack(boolean selected) {
        ItemStack stack = new ItemStack(getMaterial(), 1);
        stack.setItemMeta(getItemMeta());
        if(selected) {
            stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
        return stack;
    }

    public Material getMaterial() {
        switch (this) {
            case BLOCKS:
                return Material.CUT_SANDSTONE;
            case TOOLS:
                return Material.WOODEN_PICKAXE;
            case WEAPONS:
                return Material.WOODEN_SWORD;
            case ARMOR:
                return Material.LEATHER_HELMET;
            case FOOD:
                return Material.APPLE;
            case POTIONS:
                return Material.POTION;
            case SPECIAL:
                return Material.NETHER_STAR;
            default:
                return Material.BLACKSTONE;
        }
    }

    public String getName() {
        switch (this) {
            case BLOCKS:
                return "Blöcke";
            case TOOLS:
                return "Werkzeuge";
            case WEAPONS:
                return "Waffen";
            case ARMOR:
                return "Rüstung";
            case FOOD:
                return "Essen";
            case POTIONS:
                return "Tränke";
            case SPECIAL:
                return "Spezielle Items";
            default:
                return "";
        }
    }

    public ItemMeta getItemMeta() {
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(getMaterial());
        meta.setDisplayName("§e" + getName());
        meta.setLocalizedName("category:" + name().toLowerCase());
        meta.setLore(null);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
        return meta;
    }

    public List<ItemStack> getItems(){
        List<ItemStack> items = new ArrayList<>();
        switch (this) {
            case BLOCKS:
                items.add(getSalableItem(Material.CUT_SANDSTONE, 4, 1, true));
                items.add(getSalableItem(Material.CUT_RED_SANDSTONE, 2, 3, true));
                items.add(getSalableItem(Material.SANDSTONE_STAIRS, 1, 1, true));
                items.add(getSalableItem(Material.COBWEB, 1, 5, true));
                items.add(getSalableItem(Material.LADDER, 1, 2, true));
                items.add(getSalableItem(Material.GLASS, 1, 2, true));

                return items;
            case TOOLS:
                ItemStack woodenPickaxe = getSalableItem(Material.WOODEN_PICKAXE, 1, 5);
                woodenPickaxe.addEnchantment(Enchantment.DIG_SPEED, 2);
                items.add(woodenPickaxe);

                ItemStack stonePickaxe = getSalableItem(Material.STONE_PICKAXE, 1, 10);
                stonePickaxe.addEnchantment(Enchantment.DIG_SPEED, 2);
                items.add(stonePickaxe);

                ItemStack ironPickaxe = getSalableItem(Material.IRON_PICKAXE, 1, 25);
                ironPickaxe.addEnchantment(Enchantment.DIG_SPEED, 2);
                items.add(ironPickaxe);

                return items;
            case WEAPONS:
                ItemStack stick = getSalableItem(Material.STICK, 1, 5);
                stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                items.add(stick);

                items.add(getSalableItem(Material.WOODEN_SWORD, 1, 10));
                items.add(getSalableItem(Material.STONE_SWORD, 1, 30));
                items.add(getSalableItem(Material.IRON_SWORD, 1, 70));

                // bows - without arrows :)
                items.add(getSalableItem(Material.BOW, 1, 100));
                ItemStack bow = getSalableItem(Material.BOW, 1, 200);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
                ItemMeta meta = bow.getItemMeta();
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                bow.setItemMeta(meta);
                items.add(bow);
                bow = getSalableItem(Material.BOW, 1, 300);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
                bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
                meta = bow.getItemMeta();
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                bow.setItemMeta(meta);
                items.add(bow);

                return items;
            case ARMOR:
                items.add(getSalableItem(Material.LEATHER_CHESTPLATE, 1, 5));
                items.add(getSalableItem(Material.CHAINMAIL_CHESTPLATE, 1, 20));
                items.add(getSalableItem(Material.IRON_CHESTPLATE, 1, 40));

                ItemStack featherBoots = getSalableItem(Material.LEATHER_BOOTS, 1, 15);
                LeatherArmorMeta featherBootsMeta = (LeatherArmorMeta)featherBoots.getItemMeta();
                featherBootsMeta.setDisplayName("§eFederstiefel");
                featherBootsMeta.setColor(DyeColor.BLACK.getColor());
                featherBootsMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                featherBoots.setItemMeta(featherBootsMeta);
                featherBoots.addEnchantment(Enchantment.PROTECTION_FALL, 3);
                items.add(featherBoots);

                return items;
            case FOOD:
                items.add(getSalableItem(Material.APPLE, 1, 1));
                items.add(getSalableItem(Material.COOKED_BEEF, 1, 2));
                items.add(getSalableItem(Material.GOLDEN_APPLE, 1, 15));

                return items;
            case POTIONS:
                //potion of healing 2
                ItemStack potionOfHealing = getSalableItem(Material.POTION, 1, 10);
                PotionMeta potionOfHealingMeta = (PotionMeta)potionOfHealing.getItemMeta();
                potionOfHealingMeta.setDisplayName("§eTrank der Heilung");
                potionOfHealingMeta.setLocalizedName("salable:healing");
                potionOfHealingMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true));
                potionOfHealingMeta.setColor(DyeColor.PINK.getColor());
                potionOfHealing.setItemMeta(potionOfHealingMeta);
                items.add(potionOfHealing);


                //potion of strength 2
                ItemStack potionOfStrength = getSalableItem(Material.POTION, 1, 50);
                PotionMeta potionOfStrengthMeta = (PotionMeta)potionOfStrength.getItemMeta();
                potionOfStrengthMeta.setDisplayName("§eTraubensaft");
                potionOfStrengthMeta.setLocalizedName("salable:strength");
                potionOfStrengthMeta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60 * 20, 0), true);
                potionOfStrengthMeta.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60 * 20, 1), true);
                potionOfStrengthMeta.setColor(DyeColor.PURPLE.getColor());
                potionOfStrength.setItemMeta(potionOfStrengthMeta);
                items.add(potionOfStrength);

                return items;
            case SPECIAL:
                items.add(getSalableItem(Material.ENDER_PEARL, 1, 80));
                ItemStack trident = getSalableItem(Material.TRIDENT, 1, 40);
                ItemMeta tridentMeta = trident.getItemMeta();
                assert tridentMeta != null;
                tridentMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "trident", 0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                trident.setItemMeta(tridentMeta);
                items.add(trident);
                items.add(getSalableItem(Material.SNOWBALL, 1, 10));
                return items;
            default:
                return null;
        }
    }

    private static ItemStack getSalableItem(Material mat, int amount, int price, boolean isBlock){
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(mat);
        meta.setLore(Arrays.asList("§ePreis:§a " + price + " Punkte"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE);
        if(isBlock)meta.setLocalizedName("block:" + mat.name().toLowerCase());
        else meta.setLocalizedName("salable:" + mat.name().toLowerCase());
        ItemStack stack = new ItemStack(mat, amount);
        stack.setItemMeta(meta);
        return stack;
    }
    private static ItemStack getSalableItem(Material mat, int amount, int price){
        return getSalableItem(mat, amount, price, false);
    }
}

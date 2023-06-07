package samann.bwplugin.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import samann.bwplugin.BwPlugin;

import java.util.ArrayList;
import java.util.List;

public class SummonShop implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) return false;

        Player player = (Player) sender;
        World world = player.getWorld();
        Location loc = player.getLocation();
        loc = BwPlugin.getIntegerLocation(loc);
        WanderingTrader shop = (WanderingTrader) world.spawnEntity(loc, EntityType.WANDERING_TRADER);

        shop.setCustomName("§l§6Shop");;
        shop.setCustomNameVisible(true);
        shop.setInvulnerable(true);
        shop.setAI(false);
        shop.setCollidable(false);
        shop.setSilent(true);
        shop.setGravity(false);
        shop.setRemoveWhenFarAway(false);
        shop.setCanPickupItems(false);
        shop.setPersistent(true);
        shop.setDespawnDelay(Integer.MAX_VALUE);

        List<MerchantRecipe> recipes = new ArrayList<>();

        Material currency = Material.GOLD_NUGGET;

        //sandstone trade:
        ItemStack sandstone = new ItemStack(Material.CUT_SANDSTONE, 4);
        MerchantRecipe sandstoneRecipe = new MerchantRecipe(sandstone, Integer.MAX_VALUE);
        sandstoneRecipe.addIngredient(new ItemStack(currency, 1));
        recipes.add(sandstoneRecipe);

        //knockback stick trade:
        ItemStack knockbackStick = new ItemStack(Material.STICK, 1);
        knockbackStick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        MerchantRecipe knockbackStickRecipe = new MerchantRecipe(knockbackStick, Integer.MAX_VALUE);
        knockbackStickRecipe.addIngredient(new ItemStack(currency, 5));
        recipes.add(knockbackStickRecipe);

        //pickaxe trade:
        ItemStack pickaxe = new ItemStack(Material.WOODEN_PICKAXE, 1);
        pickaxe.addEnchantment(Enchantment.DIG_SPEED, 2);
        MerchantRecipe pickaxeRecipe = new MerchantRecipe(pickaxe, Integer.MAX_VALUE);
        pickaxeRecipe.addIngredient(new ItemStack(currency, 5));
        recipes.add(pickaxeRecipe);

        //apple trade:
        ItemStack apple = new ItemStack(Material.APPLE, 1);
        MerchantRecipe appleRecipe = new MerchantRecipe(apple, Integer.MAX_VALUE);
        appleRecipe.addIngredient(new ItemStack(currency, 1));
        recipes.add(appleRecipe);

        //red sandstone trade:
        ItemStack redSandstone = new ItemStack(Material.CUT_RED_SANDSTONE, 2);
        MerchantRecipe redSandstoneRecipe = new MerchantRecipe(redSandstone, Integer.MAX_VALUE);
        redSandstoneRecipe.addIngredient(new ItemStack(currency, 3));
        recipes.add(redSandstoneRecipe);

        //sandstone stairs trade:
        ItemStack sandstoneStairs = new ItemStack(Material.SANDSTONE_STAIRS, 1);
        MerchantRecipe sandstoneStairsRecipe = new MerchantRecipe(sandstoneStairs, Integer.MAX_VALUE);
        sandstoneStairsRecipe.addIngredient(new ItemStack(currency, 1));
        recipes.add(sandstoneStairsRecipe);

        //cobweb trade:
        ItemStack cobweb = new ItemStack(Material.COBWEB, 1);
        MerchantRecipe cobwebRecipe = new MerchantRecipe(cobweb, Integer.MAX_VALUE);
        cobwebRecipe.addIngredient(new ItemStack(currency, 5));
        recipes.add(cobwebRecipe);

        //wooden sword trade:
        ItemStack woodenSword = new ItemStack(Material.WOODEN_SWORD, 1);
        MerchantRecipe woodenSwordRecipe = new MerchantRecipe(woodenSword, Integer.MAX_VALUE);
        woodenSwordRecipe.addIngredient(new ItemStack(currency, 8));
        recipes.add(woodenSwordRecipe);


        shop.setRecipes(recipes);

        return true;
    }
}

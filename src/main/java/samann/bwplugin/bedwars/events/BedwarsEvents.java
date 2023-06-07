package samann.bwplugin.bedwars.events;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.bedwars.Bedwars;
import samann.bwplugin.bedwars.BedwarsPlayer;
import samann.bwplugin.bedwars.TeamColor;
import samann.bwplugin.bedwars.shop.Category;
import samann.bwplugin.bedwars.shop.ShopInventory;
import samann.bwplugin.games.GamePlayer;
import samann.bwplugin.games.events.GameEvent;
import samann.bwplugin.pvp.ComboPvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BedwarsEvents extends GameEvent {
    Bedwars bedwarsGame;
    ComboPvp pvp;
    public BedwarsEvents(Bedwars game) {
        bedwarsGame = game;
        pvp = new ComboPvp(game);
    }

    @EventHandler
    public void onEvent(BlockBreakEvent event) {
        if(ignoreEvent(event)) return;

        //for build mode:
        if(event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        Material mat = event.getBlock().getType();
        if (!canBreak(mat)) {
            event.setCancelled(true);
        }
        if(mat.equals(Material.DIAMOND_BLOCK)){
            bedwarsGame.addExtraLife(event.getPlayer(), event.getBlock().getLocation());
            return;
        }


        TeamColor bedColor = null;
        boolean validColor = false;
        try{
            bedColor = TeamColor.parse(mat);
            validColor = true;
        }catch (IllegalArgumentException ignored){}

        if(validColor && mat.name().endsWith("_BED")){
            event.setCancelled(!bedwarsGame.destroyBed(bedColor, event.getPlayer()));
            event.setDropItems(false);
        }

        //red sandstone will turn into sandstone without dropping anything
        if(mat.equals(Material.CUT_RED_SANDSTONE)){
            event.setDropItems(false);
            event.setCancelled(true);
            event.getBlock().setType(Material.CUT_SANDSTONE);
        }

        if(mat.equals(Material.COBWEB)){
            event.setDropItems(false);
        }
    }
    @EventHandler
    public void onEvent(BlockPlaceEvent event) {
        if(ignoreEvent(event)) return;

        BedwarsPlayer bp = bedwarsGame.getBwPlayer(event.getPlayer());
        if(bp == null) return;

        if(bedwarsGame.isTeamSpawn(event.getBlock().getLocation())){
            event.setCancelled(true);
            bp.msg("§cDu darfst keine Team-Spawns blockieren!");
        }
    }
    @EventHandler
    public void onEvent(EntityPickupItemEvent event) {
        if(ignoreEvent(event)) return;

        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BedwarsPlayer bp = bedwarsGame.getBwPlayer(player);
            Material material = event.getItem().getItemStack().getType();
            if(material == Bedwars.spawningMaterial && bp != null) {
                event.setCancelled(true);
                bp.addLevels(event.getItem().getItemStack().getAmount());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1);
                event.getItem().remove();
            }
        }
    }
    @EventHandler
    public void onEvent(EntityDamageByEntityEvent event) {
        if(ignoreEvent(event)) return;

        pvp.hit(event);
        if(event.isCancelled()) return;

        Entity source = event.getDamager();
        Entity target = event.getEntity();

        Player player = target instanceof Player ? (Player) target : null;
        Player killer = source instanceof Player ? (Player) source : null;

        if(player == null) return;

        boolean kill = player.getHealth() - event.getFinalDamage() <= 0;

        boolean accepted;
        if(killer == null) accepted =  bedwarsGame.damage(player, kill);
        else accepted = bedwarsGame.damage(player, killer, kill);



        if(!accepted || kill){
            //event.setCancelled(true);
        }else {
            player.damage(event.getFinalDamage());
        }
        event.setCancelled(true);
        /*else if(player.getNoDamageTicks() <= 0 && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
            double gravity = 32d / (20 * 20);
            double boostHeight = 0.3d;
            double verticalSpeed = Math.sqrt(2 * gravity * boostHeight);
            player.setVelocity(player.getVelocity().clone().add(new Vector(0, verticalSpeed, 0)));
        }*/
    }

    @EventHandler
    public void onEvent(EntityDamageEvent event){
        if(ignoreEvent(event)) return;
        if(event instanceof EntityDamageByEntityEvent){
            return;
        }

        Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if(player == null) return;

        if(bedwarsGame.rules.smashBw && event.getCause() == EntityDamageEvent.DamageCause.FALL){
            event.setCancelled(true);
            return;
        }

        boolean kill = player.getHealth() - event.getFinalDamage() <= 0;

        boolean accepted = bedwarsGame.damage(player, kill);

        if(!accepted || kill){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEvent(CraftItemEvent event) {
        if(ignoreEvent(event)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEvent(PlayerInteractEntityEvent event) {
        if(ignoreEvent(event)) return;

        if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
            event.setCancelled(true);
            return;
        }
        if(event.getRightClicked().getType() == EntityType.WANDERING_TRADER){
            event.setCancelled(true);
            event.getPlayer().openInventory(new ShopInventory(event.getPlayer(), Category.BLOCKS));
        }else if(event.getRightClicked() instanceof Player && event.getPlayer().isSneaking()){
            Player p1 = event.getPlayer();
            Player p2 = (Player) event.getRightClicked();
            BedwarsPlayer bp1 = bedwarsGame.getBwPlayer(p1);
            BedwarsPlayer bp2 = bedwarsGame.getBwPlayer(p2);
            if(bp1 == null || bp2 == null || bp1.team != bp2.team) return;
            int n = 10;
            if(n > p1.getLevel()) n = p1.getLevel();
            p1.setLevel(p1.getLevel() - n);
            p2.setLevel(p2.getLevel() + n);
            p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
        }
    }

    @EventHandler
    public void onStepOnPlate(PlayerInteractEvent event){
        if(ignoreEvent(event)) return;

        GamePlayer player = bedwarsGame.getPlayer(event.getPlayer());
        
        //if player steps on blackstone pressure plate, he will get a jump boost
        if(event.getPlayer().getGameMode().equals(GameMode.CREATIVE) || player.isSpectator()) return;

        if(event.getAction().equals(Action.PHYSICAL) && Objects.requireNonNull(event.getClickedBlock()).getType() == Material.POLISHED_BLACKSTONE_PRESSURE_PLATE){

            double jumpHeight = 4.9;
            double gravity = 32d / (20 * 20);
            //calculate vertical speed using formula: v = sqrt(2 * g * h)
            double verticalSpeed = Math.sqrt(2 * gravity * jumpHeight);

            if(event.getPlayer().getFallDistance() > 3){
                event.setCancelled(true);
                return;
            }

            Vector newVel;
            if(event.getPlayer().isSprinting()){
                newVel = event.getPlayer().getLocation().getDirection().multiply(event.getPlayer().getWalkSpeed() * 3).setY(verticalSpeed);
            }else{
                newVel = new Vector(0, verticalSpeed, 0);
            }
            event.getPlayer().setVelocity(newVel);
            event.getPlayer().setFallDistance(0);
        }else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType().toString().endsWith("_BED") && !event.getPlayer().isSneaking()){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEvent(PlayerItemConsumeEvent event){
        if(ignoreEvent(event)) return;

        //removes bottle after using potion
        if(event.getItem().getType() == Material.POTION){
            //remove glass bottle after one tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
                }
            }.runTaskLater(BwPlugin.instance, 1);
        }
    }
    @EventHandler
    public void onEvent(ProjectileHitEvent event){
        if(ignoreEvent(event)) return;

        if(event.getEntity() instanceof Trident){
            Trident trident = (Trident) event.getEntity();
            trident.remove();

            Location loc;
            if(event.getHitBlock() != null) loc = event.getHitBlock().getLocation();
            else loc = Objects.requireNonNull(event.getHitEntity()).getLocation();

            //strike lightning at loc
            bedwarsGame.world.strikeLightning(loc);


            final int[] destructionSpread = {20};

            List<Location> alreadyHit = new ArrayList<>();
            if(!loc.getBlock().getType().isAir()){
                alreadyHit.add(loc);
            }else {
                alreadyHit.add(searchBlock(loc, alreadyHit));
            }
            breakBlock(alreadyHit.get(0).getBlock());

            (new BukkitRunnable() {
                @Override
                public void run() {
                    //randomly picks one block from the list and spreads destruction around it
                    //the higher the index, the more likely will it be picked
                    int index = (int) ((1 - Math.random() * Math.random()) * alreadyHit.size());
                    Location locToBreak = searchBlock(alreadyHit.get(index), alreadyHit);
                    if (locToBreak != null) {
                        alreadyHit.add(locToBreak);
                        breakBlock(locToBreak.getBlock());
                    }
                    destructionSpread[0]--;
                    if (destructionSpread[0] <= 0) cancel();
                }
            }).runTaskTimer(BwPlugin.instance, 0, 1);
        }else if(event.getEntity() instanceof Snowball){
            if(event.getHitEntity() instanceof Player){
                Player player = (Player) event.getHitEntity();
                if(bedwarsGame.getBwPlayer(player) == null){
                    event.setCancelled(true);
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));
                player.playEffect(EntityEffect.HURT);
            }
        }
    }


    @EventHandler
    public void onEvent(PlayerToggleFlightEvent event){
        if(ignoreEvent(event)) return;

        event.setCancelled(true);
        if(!bedwarsGame.rules.smashBw) return;
        BedwarsPlayer player = bedwarsGame.getBwPlayer(event.getPlayer());
        if(player == null) return;
        if(!player.getBoostAvailable()) return;

        Vector direction = event.getPlayer().getLocation().getDirection().clone().normalize();
        direction.setY(Math.abs(direction.getY()));

        final double gravity = 32d / (20 * 20);
        final double minBoostHeight = 2.5d;
        final double maxBoostHeight = 2.5d;
        final double maxBoostDistance = 8;

        double minVerticalSpeed = Math.sqrt(2 * gravity * minBoostHeight);
        double maxVerticalSpeed = Math.sqrt(2 * gravity * maxBoostHeight);
        double verticalSpeed = minVerticalSpeed + direction.getY() * (maxVerticalSpeed - minVerticalSpeed);

        double time = 2 * verticalSpeed / gravity;
        double maxHorizontalSpeed = maxBoostDistance / time;

        Vector velocity = new Vector(direction.getX() * maxHorizontalSpeed, verticalSpeed, direction.getZ() * maxHorizontalSpeed);
        player.player.setVelocity(velocity);
        player.setBoostAvailable(false);
    }


    private Location searchBlock(Location startLoc, List<Location> alreadyHit){
        // searches neighbor block that is not air, in random order
        // returns null if no block found

        int x = startLoc.getBlockX();
        int y = startLoc.getBlockY();
        int z = startLoc.getBlockZ();

        Location[] neighborBlocks = new Location[6];
        neighborBlocks[0] = new Location(startLoc.getWorld(), x + 1, y, z);
        neighborBlocks[1] = new Location(startLoc.getWorld(), x - 1, y, z);
        neighborBlocks[2] = new Location(startLoc.getWorld(), x, y + 1, z);
        neighborBlocks[3] = new Location(startLoc.getWorld(), x, y - 1, z);
        neighborBlocks[4] = new Location(startLoc.getWorld(), x, y, z + 1);
        neighborBlocks[5] = new Location(startLoc.getWorld(), x, y, z - 1);

        //randomize order of neighborBlocks
        for(int i = 0; i < neighborBlocks.length; i++){
            int randomIndex = (int) (Math.random() * neighborBlocks.length);
            Location temp = neighborBlocks[i];
            neighborBlocks[i] = neighborBlocks[randomIndex];
            neighborBlocks[randomIndex] = temp;
        }

        for(Location neighborBlock : neighborBlocks){
            if(!neighborBlock.getBlock().getType().isAir() && !alreadyHit.contains(neighborBlock)){
                return neighborBlock;
            }
        }
        return null;
    }

    private boolean breakBlock(Block block){
        if(canBreak(block.getType())){
            if(block.getType().equals(Material.CUT_RED_SANDSTONE)){
                block.setType(Material.CUT_SANDSTONE);
            }else {
                if(block.getType().equals(Material.COBWEB)){
                    block.setType(Material.AIR);
                }else {
                    block.breakNaturally();
                }

            }
            return true;
        }
        return false;
    }


    public static boolean canBreak(Material mat){
        return mat.equals(Material.CUT_SANDSTONE)
                || mat.equals(Material.CUT_RED_SANDSTONE)
                || mat.equals(Material.SANDSTONE_STAIRS)
                || mat.equals(Material.COBWEB)
                || mat.equals(Material.LADDER)
                || mat.equals(Material.GLASS);
    }

}

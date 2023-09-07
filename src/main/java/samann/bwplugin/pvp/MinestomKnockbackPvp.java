package samann.bwplugin.pvp;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.Game;
import samann.bwplugin.games.events.GameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinestomKnockbackPvp extends GameEvent {
    private static final Map<UUID, Integer> noDamageTicksEntities = new HashMap<>();
    private static BukkitTask onTick;

    public MinestomKnockbackPvp(Game game){
        this.game = game;

        if(onTick == null){
            onTick = new BukkitRunnable(){
                @Override
                public void run() {
                    noDamageTicksEntities.entrySet().forEach(e -> e.setValue(e.getValue()-1));
                    noDamageTicksEntities.entrySet().removeIf(e -> e.getValue() <= 0);
                    noDamageTicksEntities.entrySet().removeIf(e -> Bukkit.getServer().getEntity(e.getKey()) == null);
                }
            }.runTaskTimer(BwPlugin.instance, 0, 1);
        }
    }
    @EventHandler
    public void onEvent(EntityDamageByEntityEvent event){
        if(ignoreEvent(event) || event.isCancelled()) return;

        hit(event);

        if(!event.isCancelled()
                && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)){
            event.setCancelled(true);
        }
    }

    public void hit(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof LivingEntity entity) || !(event.getDamager() instanceof HumanEntity damager)) return;
        if (entity instanceof ArmorStand) return;


        if(noDamageTicksEntities.containsKey(entity.getUniqueId())){
            event.setCancelled(true);
            return;
        }

        if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
            entity.setNoDamageTicks(0);
            ItemStack weapon = damager.getInventory().getItemInMainHand();

            int noDamageTicks = 10;
            if(weapon.getType() == Material.BLAZE_ROD) noDamageTicks = 3;
            noDamageTicksEntities.put(entity.getUniqueId(), noDamageTicks);

            float yaw = damager.getLocation().getYaw();

            entity.playHurtAnimation(0);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_HOSTILE_HURT, 1, 1);

            float strength = 0.5f;

            int knockbackLevel = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            strength += knockbackLevel * 0.5f;

            if(damager instanceof Player && ((Player)damager).isSprinting()) {
                strength *= 1.3;
            }

            takeKnockback(entity, strength, Math.sin(Math.toRadians(yaw)), -Math.cos(Math.toRadians(yaw)));
        }
    }

    public void takeKnockback(LivingEntity entity, float strength, final double x, final double z) {
        if (strength > 0.0F) {
            //strength *= (float) MinecraftServer.TPS;
            Vector velocityModifier = (new Vector(x, 0, z)).normalize().multiply(strength);
            double verticalLimit = 0.4;// * (double) MinecraftServer.TPS;
            entity.setVelocity(new Vector(
                    entity.getVelocity().getX() / 2.0 - velocityModifier.getX(),
                    //entity.isOnGround()
                    // ? Math.min(verticalLimit, entity.getVelocity().getY() / 2.0 + (double)strength)
                    // : entity.getVelocity().getY(),
                    entity.isOnGround()
                            ? Math.min(verticalLimit, entity.getVelocity().getY() / 2.0 + (double)strength)
                            : Math.min(verticalLimit, entity.getVelocity().getY() + (double)strength / 2.0),
                    entity.getVelocity().getZ() / 2.0 - velocityModifier.getZ()));
        }
    }
}

package samann.bwplugin.pvp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
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
import java.util.Objects;
import java.util.UUID;

public class ComboPvp extends GameEvent {
    private static final Map<UUID, Integer> noDamageTicksEntities = new HashMap<>();
    private static BukkitTask onTick;

    public ComboPvp(Game game){
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

        if(!event.isCancelled() && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)){
            event.setCancelled(true);
        }
    }

    public void hit(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof LivingEntity) || !(event.getDamager() instanceof HumanEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        HumanEntity damager = (HumanEntity) event.getDamager();
        boolean critical = damager.getFallDistance() > 0;
        if(critical) spawnParticles(Particle.CRIT_MAGIC, entity.getEyeLocation());

        if(noDamageTicksEntities.containsKey(entity.getUniqueId())){
            //if(damager instanceof Player) ((Player)damager).setSprinting(false);
            event.setCancelled(true);
            return;
        }

        if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
            entity.setNoDamageTicks(0);
            ItemStack weapon = damager.getInventory().getItemInMainHand();

            int noDamageTicks = 10;
            if(weapon.getType() == Material.BLAZE_ROD) noDamageTicks = 3;
            noDamageTicksEntities.put(entity.getUniqueId(), noDamageTicks);


            entity.playEffect(EntityEffect.HURT);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_HOSTILE_HURT, 1, 1);

            double gravity = 32d / (20 * 20);
            double boostHeight = 0.6d;
            double boostDistance = 5;

            if(damager instanceof Player && ((Player)damager).isSprinting()) {
                boostDistance *= 1.3;
                //((Player)damager).setSprinting(false);
            }

            double resistance = -((float)entity.getFreezeTicks() / entity.getMaxFreezeTicks()) * 2;
            int knockbackLevel = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            resistance -= knockbackLevel;
            boostHeight *= knockbackLevel + 1;

            double verticalSpeed = Math.sqrt(2 * gravity * boostHeight);
            double time = 2 * verticalSpeed / gravity;
            double horizontalSpeed = boostDistance / time;

            Vector direction = entity.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
            //damager.getLocation().getDirection()
            Vector knockback = direction.multiply(horizontalSpeed);
            knockback.multiply(Math.pow(0.5, resistance));
            knockback.setY(verticalSpeed);

            //entity.setFreezeTicks(Math.min(entity.getFreezeTicks() + (critical ? 25 : 35), entity.getMaxFreezeTicks()));
            if (entity.getVelocity().getY() < 0) knockback.setY(entity.getVelocity().getY() + knockback.getY());
            entity.setVelocity(knockback);
        }
    }

    private void spawnParticles(Particle particle, Location loc){
        if(loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 10);
    }
}

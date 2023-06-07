package samann.bwplugin.pvp;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

public class KnockbackCritPvp extends GameEvent {
    private static final Map<Entity, Integer> noDamageTicksEntities = new HashMap<>();
    private static BukkitTask onTick;

    public KnockbackCritPvp(Game game){
        this.game = game;

        if(onTick == null){
            onTick = new BukkitRunnable(){
                @Override
                public void run() {
                    noDamageTicksEntities.entrySet().forEach(e -> e.setValue(e.getValue()-1));
                    noDamageTicksEntities.entrySet().removeIf(e -> e.getValue() <= 0);
                }
            }.runTaskTimer(BwPlugin.instance, 0, 1);
        }
    }
    @EventHandler
    public void onEvent(EntityDamageByEntityEvent event){
        if(ignoreEvent(event) || event.isCancelled()) return;

        if(event.getEntity() instanceof LivingEntity && event.getDamager() instanceof HumanEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            HumanEntity damager = (HumanEntity) event.getDamager();

            boolean critical = damager.getFallDistance() > 0;
            if(critical) spawnParticles(Particle.CRIT_MAGIC, entity.getEyeLocation());

            if(noDamageTicksEntities.containsKey(entity)){
                if(critical && noDamageTicksEntities.get(entity) > 8){
                    noDamageTicksEntities.put(entity, 8);
                }
                if(damager instanceof Player) ((Player)damager).setSprinting(false);
                event.setCancelled(true);
                return;
            }

            if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
                event.setCancelled(true);
                entity.damage(event.getFinalDamage());
                entity.setNoDamageTicks(0);

                int noDamageTicks = 10;
                if(critical) {
                    noDamageTicks = 8;
                }
                noDamageTicksEntities.put(entity, noDamageTicks);


                entity.playEffect(EntityEffect.HURT);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_HOSTILE_HURT, 1, 1);

                double gravity = 32d / (20 * 20);
                double boostHeight = 0.6d;
                double boostDistance = 5;
                if(damager instanceof Player && ((Player)damager).isSprinting()) {
                    boostDistance *= 1.5;
                    ((Player)damager).setSprinting(false);
                }

                double resistance = (entity.getHealth() / Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) * 0.5;
                ItemStack weapon = damager.getInventory().getItemInMainHand();
                int knockbackLevel = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
                resistance -= knockbackLevel;
                boostHeight *= knockbackLevel + 1;

                double verticalSpeed = Math.sqrt(2 * gravity * boostHeight);
                double time = 2 * verticalSpeed / gravity;
                double horizontalSpeed = boostDistance / time;

                //Vector direction = entity.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
                Vector knockback = damager.getLocation().getDirection().multiply(horizontalSpeed);
                knockback.multiply(Math.pow(0.5, resistance));
                knockback.setY(verticalSpeed);

                entity.setVelocity(knockback);
            }
        }

    }


    private void spawnParticles(Particle particle, Location loc){
        if(loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 10);
    }
}

package samann.bwplugin.airwars;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.events.GameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AirwarsPvp extends GameEvent {
    private static final Map<UUID, Integer> noDamageTicksEntities = new HashMap<>();
    private static BukkitTask onTick;
    private Airwars airwars;

    public AirwarsPvp(Airwars airwars){
        this.airwars = airwars;

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

    public void hitEvent(EntityDamageByEntityEvent event){
        AirwarsPlayer target;
        AirwarsPlayer hitter;
        try {
            target = (AirwarsPlayer) airwars.getPlayer((Player) event.getEntity());
            hitter = (AirwarsPlayer) airwars.getPlayer((Player) event.getDamager());
        } catch (ClassCastException ignored) {
            return; // entities are no players, doing nothing
        }

        if (target == null || hitter == null) {
            return;
        }

        if (noDamageTicksEntities.containsKey(target.player.getUniqueId())){
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
            int noDamageTicks = 5;
            noDamageTicksEntities.put(target.player.getUniqueId(), noDamageTicks);
            target.player.getWorld().playSound(target.player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1);

            boolean riptide = hitter.player.isRiptiding();
            float yaw = hitter.player.getLocation().getYaw();
            hit(hitter, target, riptide ? 2.5 : 1, yaw);
        }
    }

    public static void hit(AirwarsPlayer hitter, AirwarsPlayer target, double baseStrength, Vector direction) {
        target.player.playHurtAnimation(0);
        target.hitBy(hitter);

        double strength = baseStrength * target.getKnockbackMultiplier();
        target.setKnockbackMultiplier(target.getKnockbackMultiplier() + baseStrength / 5);

        direction = direction.clone().multiply(-1).setY(0).normalize();

        takeKnockback(target.player, (float) strength, direction.getX(), direction.getZ());
    }

    public static void hit(AirwarsPlayer hitter, AirwarsPlayer target, double baseStrength, float yaw) {
        hit(hitter, target, baseStrength, new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))));
    }

    private static void takeKnockback(LivingEntity entity, float strength, final double x, final double z) {
        if (strength > 0.0F) {
            Vector velocityModifier = (new Vector(x, 0, z)).normalize().multiply(strength);
            double verticalLimit = 0.8;
            double verticalVelocity = entity.isOnGround()
                    ? entity.getVelocity().getY() / 2.0 + (double)strength / 2
                    : entity.getVelocity().getY() + (double)strength / 4;
            verticalVelocity = Math.min(verticalLimit, verticalVelocity);
            entity.setVelocity(new Vector(
                    entity.getVelocity().getX() / 2.0 - velocityModifier.getX(),
                    verticalVelocity,
                    entity.getVelocity().getZ() / 2.0 - velocityModifier.getZ()));
        }
    }
}

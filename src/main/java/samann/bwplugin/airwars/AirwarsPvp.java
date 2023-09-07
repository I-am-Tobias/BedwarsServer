package samann.bwplugin.airwars;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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

    public void hit(EntityDamageByEntityEvent event){
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

            target.player.playHurtAnimation(0);

            boolean riptide = hitter.player.isRiptiding();
            float strength = riptide ? 2f : 1f;
            strength *= target.knockbackMultiplier;
            float yaw = hitter.player.getLocation().getYaw();
            takeKnockback(target.player, strength, Math.sin(Math.toRadians(yaw)), -Math.cos(Math.toRadians(yaw)));
            target.hitBy(hitter);
            target.knockbackMultiplier += riptide ? 0.3 : 0.2;
        }
    }

    public static void knock(AirwarsPlayer hitter, AirwarsPlayer target, double baseStrength, Vector direction) {
        target.player.playHurtAnimation(0);
    }

    public static void takeKnockback(LivingEntity entity, float strength, final double x, final double z) {
        if (strength > 0.0F) {
            Vector velocityModifier = (new Vector(x, 0, z)).normalize().multiply(strength);
            entity.setVelocity(new Vector(
                    entity.getVelocity().getX() / 2.0 - velocityModifier.getX(),
                    entity.isOnGround()
                            ? entity.getVelocity().getY() / 2.0 + (double)strength / 2
                            : entity.getVelocity().getY() + (double)strength / 4,
                    entity.getVelocity().getZ() / 2.0 - velocityModifier.getZ()));
        }
    }
}

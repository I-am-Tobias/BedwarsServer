package samann.bwplugin.pvp;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import samann.bwplugin.games.Game;
import samann.bwplugin.games.events.GameEvent;

public class MoreJumpHeight extends GameEvent {
    public MoreJumpHeight(Game game){
        this.game = game;
    }
    @EventHandler
    public void onEvent(EntityDamageByEntityEvent event){
        if(ignoreEvent(event) || event.isCancelled()) return;

        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            if(entity.getNoDamageTicks() <= 10 && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
                double gravity = 32d / (20 * 20);
                double boostHeight = 0.3d;
                double verticalSpeed = Math.sqrt(2 * gravity * boostHeight);
                entity.setVelocity(entity.getVelocity().clone().add(new Vector(0, verticalSpeed, 0)));
            }
        }

    }
}

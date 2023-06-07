package samann.bwplugin.games.events;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import samann.bwplugin.games.Game;

public abstract class GameEvent implements Listener {
    protected Game game;

    protected boolean ignoreEvent(Event e){
        return !game.isRunning() || !checkWorld(e);
    }
    private boolean checkWorld(Event event) {
        // check for: AsyncPlayerPreLoginEvent, BlockEvent, EntityEvent, HangingEvent, InventoryEvent, InventoryMoveItemEvent, InventoryPickupItemEvent, PlayerEvent, PlayerLeashEntityEvent, PlayerPreLoginEvent, ServerEvent, TabCompleteEvent, VehicleEvent, WeatherEvent, WorldEvent
        if(event instanceof PlayerChangedWorldEvent){
            PlayerChangedWorldEvent e = (PlayerChangedWorldEvent) event;
            return e.getPlayer().getWorld().equals(game.world) || e.getFrom().equals(game.world);
        } else if (event instanceof WorldEvent) {
            return ((WorldEvent) event).getWorld().equals(game.world);
        } else if (event instanceof BlockEvent) {
            return ((BlockEvent) event).getBlock().getWorld().equals(game.world);
        } else if (event instanceof PlayerEvent) {
            return ((PlayerEvent) event).getPlayer().getWorld().equals(game.world);
        } else if (event instanceof EntityEvent) {
            return ((EntityEvent) event).getEntity().getWorld().equals(game.world);
        } else if (event instanceof InventoryEvent || event instanceof InventoryMoveItemEvent || event instanceof InventoryPickupItemEvent) {
            InventoryHolder holder;
            if (event instanceof InventoryMoveItemEvent) {
                holder = ((InventoryMoveItemEvent) event).getSource().getHolder();
            } else if (event instanceof InventoryPickupItemEvent) {
                holder = ((InventoryPickupItemEvent) event).getInventory().getHolder();
            } else {
                holder = ((InventoryEvent) event).getInventory().getHolder();
            }
            if (holder instanceof Entity) {
                return ((Entity) holder).getWorld().equals(game.world);
            } else if (holder instanceof BlockInventoryHolder) {
                return ((BlockInventoryHolder) holder).getBlock().getWorld().equals(game.world);
            } else {
                //print message that there is a not supported inventory event
                System.out.println(getClass().getName() + ": Not supported inventory event: " + event.getClass().getName());
                return false;
            }
        } else if (event instanceof WeatherEvent) {
            return ((WeatherEvent) event).getWorld().equals(game.world);
        } else if (event instanceof PlayerLeashEntityEvent) {
            return ((PlayerLeashEntityEvent) event).getPlayer().getWorld().equals(game.world);
        } else if (event instanceof PlayerPreLoginEvent) {
            return false;
        } else if (event instanceof TabCompleteEvent) {
            CommandSender cs = ((TabCompleteEvent) event).getSender();
            if (cs instanceof Entity) {
                return ((Entity) cs).getWorld().equals(game.world);
            } else if (cs instanceof BlockCommandSender) {
                return ((BlockCommandSender) cs).getBlock().getWorld().equals(game.world);
            } else {
                //print message that there is a not supported tab complete event
                System.out.println(getClass().getName() + ": Not supported tab complete event: " + event.getClass().getName());
                return false;
            }
        } else if (event instanceof HangingEvent) {
            return ((HangingEvent) event).getEntity().getWorld().equals(game.world);
        } else if (event instanceof AsyncPlayerPreLoginEvent) {
            return false;
        } else if (event instanceof ServerEvent) {
            return false;
        } else if (event instanceof VehicleEvent) {
            return ((VehicleEvent) event).getVehicle().getWorld().equals(game.world);
        } else {
            //print message that there is a not supported event
            System.out.println(getClass().getName() + ": Not supported event: " + event.getClass().getName());
            return false;
        }
    }

}

package samann.bwplugin.lobby;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import samann.bwplugin.games.Game;
import samann.bwplugin.games.GamePlayer;
import samann.bwplugin.games.events.GameEvent;
import samann.bwplugin.pvp.ComboPvp;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends Game {
    final List<GameStarter> gameStarters = new ArrayList<>();


    public Lobby(World world) {
        super(world, "Lobby");
        start();
    }

    @Override
    protected void initializeWorld() {
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setPVP(true);
    }

    @Override
    protected void onTick() {

    }

    public LobbyPlayer getLobbyPlayer(Player player) {
        GamePlayer gamePlayer = getPlayer(player);
        if (gamePlayer instanceof LobbyPlayer) {
            return (LobbyPlayer) gamePlayer;
        }
        return null;
    }

    @Override
    public void end() {
        super.end();

        while(!gameStarters.isEmpty()){
            gameStarters.get(0).reset();
        }
    }

    @Override
    public void onPlayerJoinWorld(Player player) {
        LobbyPlayer lobbyPlayer = new LobbyPlayer(player, this);
        addPlayer(lobbyPlayer);
    }

    @Override
    public void onPlayerLeaveWorld(Player player) {
        gameStarters.forEach(gameStarter -> gameStarter.removePlayer(player));//cant do that: ConcurrentModificationException
        GamePlayer gamePlayer = getPlayer(player);
        if(gamePlayer != null) {
            removePlayer(gamePlayer);
        }
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(PlayerInteractEntityEvent event) {
                if(ignoreEvent(event)) return;

                if(event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                    event.setCancelled(true);
                    return;
                }
                Player player = event.getPlayer();
                LobbyPlayer lobbyPlayer = getLobbyPlayer(player);
                if(lobbyPlayer == null) return;

                if(event.getRightClicked() instanceof Shulker) {
                    Shulker shulker = (Shulker) event.getRightClicked();
                    GameStarter gameStarter = null;
                    for(GameStarter gs : gameStarters) {
                        if(gs.shulker.equals(shulker)) {
                            gameStarter = gs;
                            break;
                        }
                    }
                    if(gameStarter == null) {
                        gameStarter = new GameStarter(shulker, Lobby.this);
                        gameStarters.add(gameStarter);
                    }
                    gameStarter.onClick(player);
                }
            }
        });
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(EntityDamageByEntityEvent event) {
                if(ignoreEvent(event)) return;

                if(event.getDamager() instanceof Player) {
                    Player player = (Player) event.getDamager();
                    if(player.getGameMode().equals(GameMode.ADVENTURE)){
                        event.setDamage(0);
                    }

                    LobbyPlayer lobbyPlayer = getLobbyPlayer(player);
                    if(lobbyPlayer == null) return;

                    if(event.getEntity() instanceof Shulker){
                        Shulker shulker = (Shulker) event.getEntity();
                        GameStarter gameStarter = null;
                        for(GameStarter gs : gameStarters) {
                            if(gs.shulker.equals(shulker)) {
                                gameStarter = gs;
                                break;
                            }
                        }
                        System.out.println("gameStarter: " + gameStarter);
                        if(gameStarter != null && gameStarter.players.contains(player)) gameStarter.start();
                    }
                }
            }
        });
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(EntityDamageEvent event){
                if(ignoreEvent(event)) return;

                if(event.getEntity() instanceof Player){
                    event.setDamage(0);
                    if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
                        event.setCancelled(true);
                    }
                }
            }
        });
        eventManager.register(new GameEvent() {
            @EventHandler
            public void onEvent(PlayerToggleFlightEvent event){
                if(ignoreEvent(event)) return;

                Player player = event.getPlayer();
                net.minecraft.world.entity.player.Player serverPlayer = ((CraftPlayer)player).getHandle();

                //riptide level:
                int k = 2;

                //perform riptide
                float f = serverPlayer.getYRot();
                float f1 = serverPlayer.getXRot();
                float f2 = -Mth.sin(f * 0.017453292F) * Mth.cos(f1 * 0.017453292F);
                float f3 = -Mth.sin(f1 * 0.017453292F);
                float f4 = Mth.cos(f * 0.017453292F) * Mth.cos(f1 * 0.017453292F);
                float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
                float f6 = 3.0F * ((1.0F + (float)k) / 4.0F);
                f2 *= f6 / f5;
                f3 *= f6 / f5;
                f4 *= f6 / f5;
                player.setVelocity(new Vector(f2, f3, f4));
                serverPlayer.startAutoSpinAttack(20);

                if (serverPlayer.onGround) {
                    float f7 = 1.1999999F;
                    serverPlayer.move(MoverType.SELF, new Vec3(0.0D, f7, 0.0D));
                }

                Sound sound;

                if (k >= 3) {
                    sound = Sound.ITEM_TRIDENT_RIPTIDE_3;
                } else if (k == 2) {
                    sound = Sound.ITEM_TRIDENT_RIPTIDE_2;
                } else {
                    sound = Sound.ITEM_TRIDENT_RIPTIDE_1;
                }

                world.playSound(player.getLocation(), sound, 1, 1);


                event.setCancelled(true);
            }
        });
        eventManager.register(new ComboPvp(this));
    }
}

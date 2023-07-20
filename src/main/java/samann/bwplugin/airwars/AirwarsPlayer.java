package samann.bwplugin.airwars;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.bedwars.BedwarsPlayer;
import samann.bwplugin.games.GamePlayer;

public class AirwarsPlayer extends GamePlayer {
  private static final double VOID_HEIGHT = 10;
  private final Airwars airwarsGame;
  private double lastPositionY;
  private AirwarsPlayer lastHitter;
  private int lastHitTime;
  private int lives = 3;
  int kills = 0;
  boolean boostAvailable = false;

  public AirwarsPlayer(Player player, Airwars game) {
    super(player, game);
    this.airwarsGame = game;
  }

  @Override
  public void onStart() {
    player.setGameMode(GameMode.SURVIVAL);
    player.setLevel(0);
    reset();
    lastPositionY = player.getLocation().getY();
  }

  @Override
  public void onEnd() {
    player.setPlayerListName(player.getName());
    player.setDisplayName(player.getName());

    msg("§lStatistiken:", true);
    msg(" - §6Deine Kills: §f" + kills, true);

    BwPlugin.resetAllAttributes(player);
  }

  private void reset() {
    if (lives > 0) {
      player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(6);
      player.setHealth(lives * 2);
    } else {
      player.setHealth(1);
    }

    player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);
    player.teleport(airwarsGame.world.getSpawnLocation().add(0.5, 0, 0.5));
    player.getOpenInventory().close();
    player.getInventory().clear();
    player.setVelocity(new Vector(0, 0, 0));
    player.setFoodLevel(20);
    player.setSaturation(6);
    player.setExp(0);
    player.setLevel(0);
    player.setFireTicks(0);
    player.setFreezeTicks(0);
    player.setFallDistance(0);
    player.setCollidable(true);
    player.setAllowFlight(false);
    player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
    player.setAbsorptionAmount(0);
    player.setNoDamageTicks(10);
    lastHitter = null;
  }

  void onTick() {
    if(player.getLocation().getY() < VOID_HEIGHT && lastPositionY > VOID_HEIGHT){
      airwarsGame.kill(this);
    }
    lastPositionY = player.getLocation().getY();
    if (lastHitTime > 0) lastHitTime--;
    if(player.isOnGround()) {
      boostAvailable = true;
      player.setAllowFlight(true);
    }
  }

  public void onDeath(){
    lives--;
    reset();
    player.playEffect(EntityEffect.HURT);
  }

  public AirwarsPlayer getKiller(){
    if(lastHitTime > 0) return lastHitter;
    return null;
  }

  public int getLives() {
    return lives;
  }

  public void updateName(){
    String name = player.getName();

    String s = "";
    s = " " + ChatColor.GOLD;
    for(int i = 0; i < lives; i++){
      s += "❤";
    }

    player.setPlayerListName(name + s);
    player.setDisplayName(name);
  }
}

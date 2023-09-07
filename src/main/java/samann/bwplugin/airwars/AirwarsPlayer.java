package samann.bwplugin.airwars;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.airwars.items.*;
import samann.bwplugin.games.GamePlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AirwarsPlayer extends GamePlayer {
  private static final int MAX_HEALTH = 3;
  private static final int LAST_HIT_TICKS = 5 * 20;
  private static final double VOID_HEIGHT = 10;
  private final Airwars airwarsGame;
  private double lastPositionY;
  private AirwarsPlayer lastHitter;
  private int lastHitTime;
  private int lives = MAX_HEALTH;
  int kills = 0;
  private boolean boostAvailable = false;
  public final List<Item> items = List.of(
          new Fireball(this),
          new Ghost(this),
          new Boots(this),
          new Trident(this),
          new FishingRod(this),
          new Anvil(this)
  );
  private final List<Runnable> itemTicks = new ArrayList<>();
  private final List<Runnable> itemTicksToRemove = new ArrayList<>();
  public double knockbackMultiplier = 1;

  public AirwarsPlayer(Player player, Airwars game) {
    super(player, game);
    this.airwarsGame = game;
  }

  @Override
  public void onStart() {
    player.setGameMode(GameMode.ADVENTURE);
    player.setLevel(0);
    player.getInventory().clear();
    for (var item : items) {
      item.reset();
      player.getInventory().addItem(item.item);
    }
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
      player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH * 2);
      player.setHealth(lives * 2);
    } else {
      player.setHealth(1);
    }
    for (var item : items) {
      item.reset();
    }

    player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);
    player.teleport(airwarsGame.world.getSpawnLocation().add(0.5, 0, 0.5));
    itemTicks.clear();
    itemTicksToRemove.clear();
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
    knockbackMultiplier = 1;
    updateName();
  }

  void onTick() {
    if(player.getLocation().getY() < VOID_HEIGHT && lastPositionY > VOID_HEIGHT){
      airwarsGame.kill(this);
    }
    lastPositionY = player.getLocation().getY();
    if (lastHitTime > 0) lastHitTime--;
    if(player.isOnGround()) {
      setBoostAvailable(true);
    }
    itemTicks.removeAll(itemTicksToRemove);
    itemTicksToRemove.clear();
    for (var itemTick : itemTicks) {
      itemTick.run();
    }
    knockbackMultiplier = Math.max(1, knockbackMultiplier - 0.05 / 20);
    updateName();
  }

  public void hitBy(AirwarsPlayer hitter) {
    if (hitter == this) return;
    lastHitTime = LAST_HIT_TICKS;
    lastHitter = hitter;
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

    String lives = " " + ChatColor.GOLD;
    for(int i = 0; i < this.lives; i++){
      lives += "❤";
    }
    DecimalFormat decimalFormat = new DecimalFormat("0.0");
    String kbStr = " §r(" + decimalFormat.format(knockbackMultiplier) + ")";

    player.setPlayerListName(name + lives + kbStr);
    player.setDisplayName(name);
  }

  public boolean getBoostAvailable() {
    return boostAvailable;
  }

  public void setBoostAvailable(boolean boostAvailable) {
    this.boostAvailable = boostAvailable;
    player.setAllowFlight(boostAvailable);
    player.setExp(boostAvailable ? 1 : 0);
  }

  public void addItemTick(Runnable itemTick) {
    itemTicks.add(itemTick);
  }

  public void removeItemTick(Runnable itemTick) {
    itemTicksToRemove.add(itemTick);
  }
}

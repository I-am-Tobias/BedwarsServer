package samann.bwplugin.airwars;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
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
  private final List<Runnable> itemTicks = new ArrayList<>();
  private final List<Runnable> itemTicksToRemove = new ArrayList<>();
  private double knockbackMultiplier = 1;
  public final List<Item> items;
  private Team team;

  public AirwarsPlayer(Player player, Airwars game) {
    super(player, game);
    this.airwarsGame = game;
    items = List.of(
            new Fireball(this),
            new Ghost(this),
            new Boots(this),
            new Trident(this),
            new FishingRod(this),
            new Anvil(this),
            new Crossbow(this)
    );
  }

  @Override
  public void onStart() {
    player.setScoreboard(airwarsGame.scoreboard);
    team = airwarsGame.scoreboard.registerNewTeam(player.getName());
    team.addPlayer(player);

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
    BwPlugin.playerRole.addPlayer(player);
    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());

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
    if(player.isOnGround()) {
      setBoostAvailable(true);
    }
    itemTicks.removeAll(itemTicksToRemove);
    itemTicksToRemove.clear();
    for (var itemTick : itemTicks) {
      itemTick.run();
    }
    var crossbow = getItem(Crossbow.class);
    if (crossbow != null) {
      crossbow.tick();
    }
    sendActionBarText();

    setKnockbackMultiplier(Math.max(1, getKnockbackMultiplier() - 0.05 / 20));

    if(player.getLocation().getY() < VOID_HEIGHT && lastPositionY > VOID_HEIGHT){
      airwarsGame.kill(this);
    } else {
      lastPositionY = player.getLocation().getY();
      if (lastHitTime > 0) lastHitTime--;
    }
  }

  public void hitBy(AirwarsPlayer hitter) {
    if (hitter == this || hitter == null) return;
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

    String lives = " " + ChatColor.AQUA;
    for(int i = 0; i < this.lives; i++){
      lives += "❤";
    }
    DecimalFormat decimalFormat = new DecimalFormat("0.0");
    String color = "§r";
    if (knockbackMultiplier >= 2) {
      color = "§6";
    }
    if (knockbackMultiplier >= 3) {
      color = "§c";
    }
    String kbStr = color +  " (" + decimalFormat.format(knockbackMultiplier) + ")";

    player.setPlayerListName(name + lives + kbStr);
    player.setDisplayName(name);
    team.setSuffix(lives + kbStr);
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

  @Nullable
  public <T> T getItem(Class<T> itemType) {
    for (var item : items) {
      if (itemType.isInstance(item)) {
        return itemType.cast(item);
      }
    }
    return null;
  }

  public double getKnockbackMultiplier() {
    return knockbackMultiplier;
  }

  public void setKnockbackMultiplier(double newValue) {
    knockbackMultiplier = newValue;
    updateName();
  }

  private void sendActionBarText() {
    Progress progress = null;
    for (var item : items) {
      if (item.isItem(player.getInventory().getItemInMainHand())) {
        progress = item.currentProgress();
        break;
      }
    }
    if (progress == null) {
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
      return;
    }
    char toRepeat = '|';
    int count = 20;
    StringBuilder text = new StringBuilder("" + switch (progress.state()) {
      case COOLDOWN -> ChatColor.RED;
      case ACTIVE -> ChatColor.YELLOW;
      case READY -> ChatColor.GREEN;
    });
    boolean colored = true;
    for (int i = 0; i < count; i++) {
      if (colored) {
        if (((double) i) / count >= progress.progress()) {
          colored = false;
          text.append(ChatColor.GRAY);
        }
      }
      text.append(toRepeat);
    }
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text.toString()));
  }
}

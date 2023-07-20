package samann.bwplugin.airwars;

import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import samann.bwplugin.games.Game;
import samann.bwplugin.games.Spectator;
import samann.bwplugin.games.events.GameEvent;

public class Airwars extends Game {
  final static String PREFIX = "§bAirwars §8» §r";
  public Airwars(World world) {
    super(world, PREFIX);
  }

  @Override
  protected void initializeWorld() {
    world.setDifficulty(Difficulty.PEACEFUL);
    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    world.setTime(1000);
    world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    world.setClearWeatherDuration(1);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    world.setGameRule(GameRule.DO_FIRE_TICK, false);
    world.setGameRule(GameRule.MOB_GRIEFING, false);
    world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
    world.setSpawnLocation(0, 30, 0);
  }

  @Override
  protected void onTick() {
    getPlayers(AirwarsPlayer.class).forEach(AirwarsPlayer::onTick);
  }

  void kill(AirwarsPlayer target){
    AirwarsPlayer killer = target.getKiller();

    if(killer == null){
      msgAll(target.player.getDisplayName() + "§7 ist gestorben!", true);
    }else{
      msgAll(target.player.getDisplayName() + "§7 wurde von " + killer.player.getDisplayName() + "§7 getötet!", true);
      killer.kills++;
      world.playSound(target.player.getLocation(), Sound.ENTITY_GUARDIAN_HURT, 10f, 1f);
    }

    target.onDeath();

    boolean finalKill = target.getLives() <= 0;
    if(finalKill) {
      addPlayer(new Spectator(target.player, this));
    }
  }

  @Override
  public void registerEvents() {
    super.registerEvents();

    eventManager.register(new AirwarsEvents(this));
  }
}

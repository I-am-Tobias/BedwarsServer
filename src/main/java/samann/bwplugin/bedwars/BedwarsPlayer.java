package samann.bwplugin.bedwars;


import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.games.GamePlayer;

public class BedwarsPlayer extends GamePlayer {
    public final Bedwars bedwarsGame;
    public final TeamColor team;

    private int kills = 0;
    private int bedsDestroyed = 0;

    private double lastPositionY;

    static final int maxExtraLives = 3;
    private int extraLives = 0;

    //last Bedwars Player who hit this player and the time since then
    private BedwarsPlayer lastHitter;
    private int lastHitTime;
    private static final int hitterGetsKillTime = 20 * 5;

    private boolean boostAvailable = false;  // for smash bw

    public BedwarsPlayer(Player player, TeamColor team, Bedwars bedwarsGame) {
        super(player, bedwarsGame);
        this.bedwarsGame = bedwarsGame;
        this.team = team;
        lastPositionY = player.getLocation().getY();
        updateName();
    }

    @Override
    public void onStart(){
        player.setGameMode(GameMode.SURVIVAL);
        player.setLevel(0);
        reset();
        lastPositionY = player.getLocation().getY();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
    }

    public void onTick(){
        if(player.getLocation().getY() < bedwarsGame.mapData.customVoidHeight && lastPositionY > bedwarsGame.mapData.customVoidHeight){
            bedwarsGame.kill(this);
        }
        lastPositionY = player.getLocation().getY();

        if(lastHitTime > 0) lastHitTime -= 1;

        //smash bw
        if(bedwarsGame.rules.smashBw){
            if(((Entity)player).isOnGround()) setBoostAvailable(true);
        }
        //if(player.getName().contains("zSamann")) Bukkit.getLogger().info(player.getVelocity().toString());
    }

    public void reset(){
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);
        player.teleport(bedwarsGame.teamSpawn(team));
        player.getOpenInventory().setCursor(null);
        player.getOpenInventory().close();
        player.getInventory().clear();
        player.setVelocity(new Vector(0, 0, 0));
        player.setFoodLevel(20);
        player.setSaturation(6);
        player.setExp(0);
        player.setLevel(player.getLevel() / 4);
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        player.setFallDistance(0);
        player.setCollidable(true);
        player.setAllowFlight(false);
        //player.getCollidableExemptions().clear();
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.setAbsorptionAmount(0);
        player.setNoDamageTicks(10);
        lastHitter = null;
    }
    public void addLevels(int amount){
        player.setLevel(player.getLevel() + amount);
    }

    public boolean useExtraLife(){
        if(extraLives > 0){
            extraLives--;
            //if position y under bedwarsGame.customVoidHeight reset player to spawn pos
            if(player.getLocation().getY() < bedwarsGame.mapData.customVoidHeight){
                player.teleport(bedwarsGame.teamSpawn(team));
                player.setFallDistance(0);
            }
            player.setHealth(10);
            player.playEffect(EntityEffect.TOTEM_RESURRECT);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1));

            return true;
        }
        return false;
    }
    public boolean addExtraLife(){
        if(extraLives < maxExtraLives){
            extraLives++;
            updateName();
            return true;
        }
        return false;
    }

    public int getExtraLives(){
        return extraLives;
    }

    public BedwarsPlayer getKiller(){
        if(lastHitTime > 0) return lastHitter;
        return null;
    }
    public void onKill(boolean finalKill){
        if(finalKill) {
            addKill();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 2f);
        }else{
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1f);
        }
    }
    public void onEnemyExtraLife(){//when player kills someone but he has extra life
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.7f, 1f);
    }
    public void onDeath(boolean finalDeath){
        if(finalDeath) {
            player.teleport(bedwarsGame.world.getSpawnLocation());
        }else reset();

        player.playEffect(EntityEffect.HURT);
    }
    @Override
    public void onEnd(){
        player.setPlayerListName(player.getName());
        player.setDisplayName(player.getName());
        BwPlugin.playerRole.addPlayer(player);

        msg("§lStatistiken:", true);
        msg(" - §6Deine Kills: §f" + getKills(), true);
        msg(" - §6Betten zerstört: §f" + getBedsDestroyed(), true);

        BwPlugin.resetAllAttributes(player);
    }

    public void DamagedBy(BedwarsPlayer attacker){
        if(attacker == null) return;
        lastHitter = attacker;
        lastHitTime = hitterGetsKillTime;
    }
    public void updateName(){
        String name = team.colorCode() + player.getName();

        String s = "";
        if(extraLives > 0){
            s = " " + ChatColor.GOLD.toString();
            for(int i = 0; i < extraLives; i++){
                s += "❤";
            }
        }

        player.setPlayerListName(name + s);
        player.setDisplayName(name);
    }


    private void addKill(){
        kills++;
    }
    public int getKills(){
        return kills;
    }
    public void onDestroyBed(){
        bedsDestroyed++;
    }
    public int getBedsDestroyed(){
        return bedsDestroyed;
    }

    public boolean getBoostAvailable(){
        return boostAvailable;
    }
    public void setBoostAvailable(boolean value){
        boostAvailable = value;
        player.setAllowFlight(value);
    }
}

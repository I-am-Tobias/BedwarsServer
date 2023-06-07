package samann.bwplugin.bedwars;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.bedwars.events.BedwarsEvents;
import samann.bwplugin.bedwars.events.ShopEvents;
import samann.bwplugin.bedwars.stats.BedwarsStats;
import samann.bwplugin.bedwars.stats.GameStats;
import samann.bwplugin.games.Game;
import samann.bwplugin.games.GamePlayer;
import samann.bwplugin.games.Spectator;
import samann.bwplugin.pvp.ComboPvp;
import samann.bwplugin.pvp.KnockbackCritPvp;
import samann.bwplugin.pvp.MoreJumpHeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bedwars extends Game {
    public final static Material spawningMaterial = Material.GOLD_NUGGET;
    final static String PREFIX = "§6Bedwars §8» §r";


    public final MapData mapData;
    public Rules rules;

    private int tick = 0;

    private int tickMultiplier = 1;//for spawners
    private final List<BlockCooldown> blockCooldowns = new ArrayList<>();

    private boolean stats = true;
    private final List<GameStats> gameStats = new ArrayList<>();



    public Bedwars(World world) {
        this(new MapData(world), world);
    }
    public Bedwars(MapData mapData, World world) {
        super(world, PREFIX);
        this.rules = Rules.staticRules;
        this.mapData = mapData;
    }

    @Override
    protected void initializeWorld(){
        world.setDifficulty(Difficulty.HARD);
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
    public void start(){
        super.start();
        if(running) return;
        running = true;

        for(Player player : world.getPlayers()){
            createNewScoreboard(player);
        }
        updateGUI();

        msgAll(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Das Spiel hat begonnen!" + (stats ? " §r§6(gewertet)" : ""), true);
    }
    @Override
    public void end(){
        super.end();
        updateGUI();
    }

    public boolean damage(Player victim, Player attacker, boolean kill){
        if(!isRunning()) return true;
        BedwarsPlayer bpVictim = getBwPlayer(victim);
        BedwarsPlayer bpAttacker = getBwPlayer(attacker);

        if(bpVictim == null || bpAttacker == null || bpVictim.team == bpAttacker.team) return false;

        bpVictim.DamagedBy(bpAttacker);
        if(kill) kill(bpVictim);
        return true;
    }
    public boolean damage(Player victim, boolean kill){
        if(!isRunning()) return true;
        BedwarsPlayer bpVictim = getBwPlayer(victim);
        if(bpVictim == null) return false;
        if(kill) kill(bpVictim);
        return true;
    }


    void kill(BedwarsPlayer target){
        BedwarsPlayer killer = target.getKiller();

        if(target.useExtraLife()){
            if(killer != null) killer.onEnemyExtraLife();

            updateGUI();
            return;
        }

        boolean finalKill = !teamData(target.team).hasBed();


        if(killer == null){
            msgAll(target.player.getDisplayName() + "§7 ist gestorben!", true);
        }else{
            String hpLeft = "";
            int hp = (int)Math.ceil(killer.player.getHealth());
            hpLeft += hp / 2 + ",";
            hpLeft += (hp % 2) * 5;
            hpLeft = "§7[§c" + hpLeft + "❤§7]";
            msgAll(target.player.getDisplayName() + "§7 wurde von " + killer.player.getDisplayName() + " " + hpLeft + "§7 getötet!", true);
            killer.onKill(finalKill);
        }

        target.onDeath(finalKill);

        if(finalKill) {
            addPlayer(new Spectator(target.player, this));
        }
    }

    @Override
    public void addPlayer(GamePlayer player) {
        super.addPlayer(player);
        if(player instanceof BedwarsPlayer) {
            gameStats.removeIf(stats -> stats.player == player);
            gameStats.add(new GameStats((BedwarsPlayer)player));
        }
    }

    public void addExtraLife(Player p, Location loc){
        BedwarsPlayer bp = getBwPlayer(p);
        if(bp == null) return;

        boolean success = bp.addExtraLife();
        if(success) {
            msgAll(p.getDisplayName() + "§7 hat ein §6§lExtra Leben§7 erhalten!", true);
            world.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            blockCooldowns.add(new BlockCooldown(loc));
            updateGUI();
        }else{
            bp.msg("§cDu hast die maximale Anzahl von Extra Leben erreicht!");
        }
    }

    public void autoTeam(List<Player> playersToAssign){
        int minTeams = (playersToAssign.size() - 1) / 4 + 1;
        if(minTeams < 2) minTeams = 2;
        int maxTeams = Math.min(playersToAssign.size(), TeamColor.values().length);
        if(minTeams > maxTeams) minTeams = maxTeams;

        //select random number of teams
        int numberOfTeams = (int)Math.floor(Math.random() * (maxTeams - minTeams + 1)) + minTeams;

        //select random teams without duplicates
        List<TeamColor> teamsLeft = new ArrayList<>(Arrays.asList(TeamColor.values()));
        List<TeamColor> teams = new ArrayList<>();
        for(int i = 0; i < numberOfTeams; i++){
            int index = (int)Math.floor(Math.random() * teamsLeft.size());
            teams.add(teamsLeft.get(index));
            teamsLeft.remove(index);
        }

        //assign players to teams
        for (Player p : playersToAssign){
            if(getBwPlayer(p) != null) continue;

            List<TeamColor> possibleTeams = new ArrayList<>();
            int leastPlayers = players.size() + 1;
            for (TeamColor c : teams){
                if(leastPlayers > playersOfTeam(c).size()){
                    leastPlayers = playersOfTeam(c).size();
                    possibleTeams.clear();
                    possibleTeams.add(c);
                }else if(leastPlayers == playersOfTeam(c).size()){
                    possibleTeams.add(c);
                }
            }
            TeamColor team = possibleTeams.get((int)(Math.random() * possibleTeams.size()));

            assignTeam(p, team);
        }
    }



    @Nullable
    public BedwarsPlayer getBwPlayer(Player player){
        for(BedwarsPlayer bp : getPlayers(BedwarsPlayer.class)){
            if(bp.player.equals(player)){
                return bp;
            }
        }
        return null;
    }

    @Override
    public void onPlayerLeaveWorld(Player player){
        if(getBwPlayer(player) != null) msgAll(player.getDisplayName() + "§7 hat das Spiel verlassen!", true);
        super.onPlayerLeaveWorld(player);
    }
    @Override
    public void onPlayerQuit(Player player){
        if(getBwPlayer(player) != null) msgAll(player.getDisplayName() + "§7 hat das Spiel verlassen!", true);
        super.onPlayerQuit(player);
    }
    @Override
    public void removePlayer(@Nullable GamePlayer player){
        super.removePlayer(player);

        if(!(player instanceof BedwarsPlayer)) return;
        BedwarsPlayer bp = (BedwarsPlayer)player;

        if(isRunning()){
            //team drop out
            if(playersOfTeam(bp.team).size() == 0){
                msgAll("§4Team " + bp.team.displayName(true) + "§4 ist ausgeschieden!", true);

                int numTeams = 0;
                for(TeamColor t : TeamColor.values()){
                    if(playersOfTeam(t).size() > 0){
                        numTeams++;
                    }
                }
                if(numTeams <= 1){
                    TeamColor winnerTeam = getPlayers(BedwarsPlayer.class).size() > 0 ? getPlayers(BedwarsPlayer.class).get(0).team : TeamColor.RED;
                    msgAll("§e§lTeam " + winnerTeam.colorCode() + "§l" + winnerTeam.displayName(false) + "§e§l hat das Spiel gewonnen!", true);

                    if(stats && gameStats.size() > 1){
                        int statsForWinner = 0;
                        int numWinners = 0;
                        for (GameStats stats : gameStats) {
                            if (stats.player.team != winnerTeam) {
                                int amount = (int) (0.1 * BedwarsStats.getStats(stats.player.player));
                                statsForWinner += amount;
                            }else{
                                numWinners++;
                            }
                        }
                        for(GameStats stats : gameStats){
                            int amount = stats.player.getKills() * 10 + stats.player.getBedsDestroyed() * 25;
                            if(stats.player.team == winnerTeam){
                                amount += statsForWinner / numWinners;
                            }else{
                                amount -= (int) (0.1 * BedwarsStats.getStats(stats.player.player));
                            }

                            if(amount < 0){
                                stats.player.msg("§7Du verlierst §c" + (-amount) + "§7 Punkte!", true);
                            }else{
                                stats.player.msg("§7Du erhälst §a" + amount + "§7 Punkte!", true);
                            }
                            BedwarsStats.addStats(stats.player.player, amount);
                        }
                    }

                    end();
                }
            }
        }
        updateGUI();
    }
    public void assignTeam(Player player, TeamColor team){
        BedwarsPlayer bp = new BedwarsPlayer(player, team, this);
        addPlayer(bp);

        int maxTeamSize = 0;
        for(TeamColor t : TeamColor.values()){
            if(playersOfTeam(t).size() > maxTeamSize){
                maxTeamSize = playersOfTeam(t).size();
            }
        }
        tickMultiplier = maxTeamSize / 2 + 1;
        updateGUI();
    }

    public boolean toggleSpawner(Location loc){
        for(Spawner s : mapData.spawners){
            if(s.getPosition().equals(loc.toVector())){
                removeSpawner(loc);
                return false;
            }
        }
        addSpawner(loc);
        return true;
    }
    private void addSpawner(Location loc){
        mapData.spawners.add(new Spawner(loc));
    }
    private void removeSpawner(Location loc){
        mapData.spawners.removeIf(spawner -> spawner.getPosition().equals(loc.toVector()));
    }

    public boolean isTeamSpawn(Location blockLocation){
        Vector blockPos = blockLocation.toVector();
        for(TeamData t : mapData.teamData){
            if(playersOfTeam(t.color()).size() == 0) continue;
            Vector spawn = t.getSpawn(world).toVector();
            spawn.setX(Math.floor(spawn.getX()));
            spawn.setY(Math.floor(spawn.getY()));
            spawn.setZ(Math.floor(spawn.getZ()));

            Vector spawnTop = spawn.clone().add(new Vector(0, 1, 0));

            if(blockPos.equals(spawn) || blockPos.equals(spawnTop)){
                return true;
            }
        }
        return false;
    }
    public List<BedwarsPlayer> playersOfTeam(TeamColor team){
        List<BedwarsPlayer> players = new ArrayList<>();
        for(BedwarsPlayer bp : getPlayers(BedwarsPlayer.class)){
            if(bp.team == team){
                players.add(bp);
            }
        }
        return players;
    }

    @Override
    protected void onTick(){
        for(Spawner spawner : mapData.spawners){
            spawner.onTick(world, tickMultiplier);
        }
        for(BlockCooldown bc : blockCooldowns){
            bc.onTick(tickMultiplier);
        }
        blockCooldowns.removeIf(BlockCooldown::isFinished);
        for(BedwarsPlayer bp : getPlayers(BedwarsPlayer.class)){
            bp.onTick();
        }

        tick++;
        if(tick % 20 == 0){
            updateGUI();
            tick = 0;
        }
    }

    public void msgTeam(String message, TeamColor team, boolean prefix){
        for(BedwarsPlayer p : playersOfTeam(team)){
            p.player.sendMessage((prefix?PREFIX:"") + message);
        }
    }
    public void msgTeam(String message, TeamColor team){
        msgTeam(message, team, false);
    }

    public TeamData teamData(TeamColor team){
        return mapData.teamData[team.ordinal()];
    }
    public boolean destroyBed(TeamColor team, Player player){
        BedwarsPlayer bp = getBwPlayer(player);
        if(bp == null || !teamData(team).hasBed()) return false;

        if(bp.team == team){
            bp.msg("§cDu darfst dein eigenes Bett nicht zerstören!");
            return false;
        }

        teamData(team).destroyBed();
        bp.onDestroyBed();

        msgAll(player.getDisplayName() + "§7 hat das §4§lBett§7 von Team " + team.displayName(true) + "§7 zerstört!", true);
        world.getPlayers().forEach(p -> {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1f);
            if(getBwPlayer(p) != null && getBwPlayer(p).team == team){
                p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.6f, 1f);
            }
        });
        updateGUI();
        return true;
    }

    private void updateGUI(){
        for(Player p : world.getPlayers()){
            BedwarsPlayer bp = getBwPlayer(p);

            if(bp != null) bp.updateName();

            updateScoreboard(p);

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, getActionBarText(bp));
        }
    }

    private TextComponent getActionBarText(BedwarsPlayer bp){
        if(bp == null){
            return new TextComponent("");
        }else{
            String extraLives = "";
            if(bp.getExtraLives() > 0){
                extraLives = ChatColor.GOLD.toString();
                for(int i = 0; i < bp.getExtraLives(); i++){
                    extraLives += "❤";
                }
            }
            String msg = bp.team.colorCode() + "Team: " + bp.team.displayName(true);
            if(bp.getExtraLives() > 0){
                msg = extraLives + " " + msg + " " + extraLives;
            }
            return new TextComponent(msg);
        }
    }

    private void updateScoreboard(Player p){
        if(!isRunning()) return;
        Scoreboard scoreboard = p.getScoreboard();
        Objective sidebarObj = scoreboard.getObjective("BedwarsSidebar");

        if(sidebarObj == null || scoreboard.getTeam("team_0") == null){
            createNewScoreboard(p);
            return;
        }

        BedwarsPlayer bp = getBwPlayer(p);

        for(TeamColor c : TeamColor.values()){
            for(int i = 0; i <= BedwarsPlayer.maxExtraLives; i++){
                Team team = scoreboard.getTeam(c.name() + "_" + i);
                team.getEntries().forEach(team::removeEntry);
                for(BedwarsPlayer bp_ : playersOfTeam(c)){
                    if(bp_.getExtraLives() == i) team.addEntry(bp_.player.getName());
                }
            }


            Team sidebarTeam = scoreboard.getTeam("team_" + c.ordinal());
            String prefix = teamData(c).hasBed ? " §a✔ " : " §c✕ ";
            if(playersOfTeam(c).size() == 0) prefix = " §7- ";
            sidebarTeam.setPrefix(prefix);

            String bold = (bp != null && bp.team == c) ? ChatColor.BOLD.toString() : "";
            String suffix = c.colorCode() + bold + c.displayName() + " §7(" + playersOfTeam(c).size() + ") ";
            sidebarTeam.setSuffix(suffix);
        }
    }
    private void createNewScoreboard(Player p){
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        for (TeamColor c : TeamColor.values()) {
            for(int i = 0; i <= BedwarsPlayer.maxExtraLives; i++){
                Team team = scoreboard.registerNewTeam(c.name() + "_" + i);
                team.setColor(c.chatColor());

                String s = "";
                if(i > 0){
                    s = " " + ChatColor.GOLD;
                    for(int j = 0; j < i; j++){
                        s += "❤";
                    }
                }
                team.setSuffix(s);

                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }

            Team sidebarTeam = scoreboard.registerNewTeam("team_" + c.ordinal()); // team_0 for example
            sidebarTeam.setColor(c.chatColor());
            sidebarTeam.addEntry(c.colorCode() + "§r");
        }

        Objective sidebarObj = scoreboard.registerNewObjective("BedwarsSidebar", "dummy", "      §6§lBedwars      ");
        sidebarObj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = 15;
        sidebarObj.getScore(" ").setScore(score--);
        for(TeamColor c : TeamColor.values()){
            sidebarObj.getScore(c.colorCode() + "§r").setScore(score--);
        }
        sidebarObj.getScore("  ").setScore(score--);

        p.setScoreboard(scoreboard);
        updateScoreboard(p);
    }

    public Location teamSpawn(TeamColor team){
        return teamData(team).getSpawn(world);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        eventManager.register(new BedwarsEvents(this));
        //eventManager.register(new MoreJumpHeight(this));
        //eventManager.register(new ComboPvp(this));
        eventManager.register(new ShopEvents());
    }

    //cooldown for extra lives
    private class BlockCooldown {
        private static final int COOLDOWN_TIME = 20*60;
        public Location loc;
        public int cooldown = 0;

        public BlockCooldown(Location loc) {
            cooldown = COOLDOWN_TIME;
            this.loc = loc;
            world.getBlockAt(loc).setType(Material.NETHERITE_BLOCK);
        }

        public void onTick(int multiplier) {
            cooldown -= multiplier;
            if(cooldown <= 0){
                world.getBlockAt(loc).setType(Material.DIAMOND_BLOCK);
                world.playSound(loc, Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);
            }
        }
        public boolean isFinished() {
            return cooldown <= 0;
        }

        public void onDestroy(){
            onTick(COOLDOWN_TIME);
        }
    }
}

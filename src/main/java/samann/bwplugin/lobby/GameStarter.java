package samann.bwplugin.lobby;

import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import samann.bwplugin.bedwars.BedwarsCreator;

import java.util.ArrayList;
import java.util.List;

public class GameStarter {
    final Lobby lobby;
    final Shulker shulker;
    final String originalName;
    final List<Player> players = new ArrayList<>();
    int countdown = 20*30;


    public GameStarter(Shulker shulker, Lobby lobby) {
        this.shulker = shulker;
        this.lobby = lobby;
        this.originalName = shulker.getCustomName();
    }

    public void onClick(Player player) {
        if(players.contains(player)) {
            removePlayer(player);
        }else{
            addPlayer(player);
        }
    }
    public void addPlayer(Player player){
        if(!players.contains(player)) {
            lobby.gameStarters.forEach(gameStarter -> gameStarter.players.remove(player));
            players.add(player);
            shulker.setPeek(0.3f);

            updateName();
        }
    }
    public void removePlayer(Player player){
        if(players.contains(player)) {
            players.remove(player);
            if(players.isEmpty()){
                reset();
            }
        }
    }

    void onTick() {
        if(countdown > 0) {
            countdown--;
            if(countdown == 0) {
                start();
            }
        }else{
            reset();
        }
    }
    void start(){
        List<Player> playersCopy = new ArrayList<>(players);
        reset();
        if(playersCopy.isEmpty()) return;

        List<String> maps = BedwarsCreator.getMapNames();
        if(maps.isEmpty()){
            reset();
            return;
        }
        String map = maps.get((int) (Math.random() * maps.size()));
        BedwarsCreator.create(map, playersCopy);
    }
    void reset(){
        shulker.setPeek(0);
        players.clear();
        shulker.setCustomName(originalName);
        lobby.gameStarters.remove(this);
    }

    void updateName(){
        shulker.setCustomName("§l" + getGameName() + "§r §a" + players.size() + " online");
        //shulker.getWorld().entit
    }


    private String getGameName(){
        return "Bedwars";
    }
}

package samann.bwplugin.bedwars.stats;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import samann.bwplugin.BwPlugin;

import java.io.*;
import java.util.*;

public abstract class BedwarsStats {
    static Map<UUID, Integer> stats = new HashMap<>();

    public static void addStats(Player player, int amount) {
        stats.put(player.getUniqueId(), stats.getOrDefault(player.getUniqueId(), 0) + amount);
    }

    public static int getStats(OfflinePlayer player) {
        return stats.getOrDefault(player.getUniqueId(), 0);
    }
    public static int getStats(UUID id) {
        return stats.getOrDefault(id, 0);
    }

    public static void resetStats(Player player) {
        stats.put(player.getUniqueId(), 0);
    }

    public static void resetStats() {
        stats.clear();
    }

    public static List<UUID> allPlayers() {
        return new ArrayList<>(stats.keySet());
    }

    public static void saveStats(){
        try {
            File dataFolder = BwPlugin.instance.getDataFolder();
            if(!dataFolder.exists())
                if(!dataFolder.mkdir())
                    return;

            FileOutputStream fos = new FileOutputStream(dataFolder.getAbsolutePath() + "//bedwars.stats");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(stats);
            oos.close();
            fos.close();
            System.out.println("Saved bedwars stats");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadStats(){
        try {
            FileInputStream fis = new FileInputStream(BwPlugin.instance.getDataFolder().getAbsolutePath() + "//bedwars.stats");
            ObjectInputStream ois = new ObjectInputStream(fis);
            stats = (Map<UUID, Integer>) ois.readObject();
            ois.close();
            fis.close();
            System.out.println("Loaded bedwars stats");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            stats = new HashMap<>();
        }
    }

    public static int getRank(OfflinePlayer player){
        return getRank(player.getUniqueId());
    }
    public static int getRank(UUID id){
        int rank = 1;
        int playerStats = getStats(id);
        for(UUID otherId : stats.keySet()){
            if(stats.get(otherId) > playerStats) rank++;
        }
        return rank;
    }

    public static List<OfflinePlayer> getTopPlayers(int amount){
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        List<UUID> allPlayers = new ArrayList<>(stats.keySet());
        allPlayers.sort((o1, o2) -> stats.get(o2) - stats.get(o1));
        for(int i = 0; i < amount && i < allPlayers.size(); i++){
            topPlayers.add(Bukkit.getOfflinePlayer(allPlayers.get(i)));
        }
        return topPlayers;
    }
}

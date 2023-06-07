package samann.bwplugin.bedwars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class BedwarsCreator {
    public static void create(String mapName, List<Player> players){
        MapData mapData;
        try{
            FileInputStream fis = new FileInputStream(mapName + "/bedwars.mapData");
            ObjectInputStream ois = new ObjectInputStream(fis);
            mapData = (MapData) ois.readObject();
            ois.close();
            fis.close();
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Bedwars game = mapData.initGame(mapName);
        game.autoTeam(players);
        game.start();
    }


    public static List<String> getMapNames(){
        File worldContainer = Bukkit.getWorldContainer();
        File[] worldDirs = worldContainer.listFiles();
        List<String> list = new ArrayList<String>();
        assert worldDirs != null;
        for(File f : worldDirs){
            if(f.isDirectory()){
                if(new File(f, "bedwars.mapData").exists()){
                    list.add(f.getName());
                }
            }
        }
        return list;
    }
}

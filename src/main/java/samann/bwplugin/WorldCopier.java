package samann.bwplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class WorldCopier {
    private static void copyFileStructure(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock", "bedwars.mapData", "playerdata", "stats"));
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists())
                        if (!target.mkdirs())
                            throw new IOException("Couldn't create world directory!");
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void deleteFolder(String path) {
        File file = new File(path);
        if(file.exists()) {
            if(file.isDirectory()) {
                for(File f : Objects.requireNonNull(file.listFiles()))
                    deleteFolder(f.getAbsolutePath());
            }else{
                file.delete();
            }
        }
    }
    public static void unloadWorld(World world) {
        if (world != null) {
            world.getPlayers().forEach(BwPlugin::sendToMainLobby);
            boolean success = Bukkit.unloadWorld(world, false);
            if(!success)
                throw new RuntimeException("Couldn't unload world!");
        }
    }
    public static World copyWorld(String originalWorldName, String newWorldName) {
        World world = Bukkit.getWorld(originalWorldName);
        if(world != null) {
            unloadWorld(world);
            world.save();
        }

        if(Bukkit.getWorld(newWorldName) != null) {
            int i = 1;
            while (Bukkit.getWorld(newWorldName + i) != null) {
                i++;
            }
            newWorldName += i;
        }

        File originalWorldFolder = new File(Bukkit.getWorldContainer(), originalWorldName);
        File newWorldFolder = new File(Bukkit.getWorldContainer(), newWorldName);

        deleteFolder(newWorldFolder.getAbsolutePath());
        copyFileStructure(originalWorldFolder, newWorldFolder);
        return new WorldCreator(newWorldName).createWorld();
    }
}

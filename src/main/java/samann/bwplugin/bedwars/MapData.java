package samann.bwplugin.bedwars;

import org.bukkit.World;
import samann.bwplugin.WorldCopier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapData implements Serializable {
    public final String world;

    int customVoidHeight = 0;
    public final TeamData[] teamData;
    public final List<Spawner> spawners = new ArrayList<>();

    public MapData(World world) {
        this.world = world.getName();

        teamData = new TeamData[TeamColor.values().length];
        for(TeamColor c : TeamColor.values()) {
            teamData[c.ordinal()] = new TeamData(c, world);
        }
    }

    public Bedwars initGame() {
        World newWorld = WorldCopier.copyWorld(world, "bedwars");

        return new Bedwars(this, newWorld);
    }
    public Bedwars initGame(String world) {
        World newWorld = WorldCopier.copyWorld(world, "bedwars");

        return new Bedwars(this, newWorld);
    }
}

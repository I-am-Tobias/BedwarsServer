package samann.bwplugin.bedwars;

import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

public class TeamData implements Serializable {
    private final TeamColor color;
    private SVector spawnPosition;
    private SVector spawnDirection;

    //private Location bedLocation;
    boolean hasBed;

    public TeamData(TeamColor color, World world){
        this.color = color;
        spawnPosition = new SVector(world.getSpawnLocation().toVector());
        spawnDirection = new SVector(world.getSpawnLocation().getDirection());
        hasBed = true;
    }

    public void setSpawn(Location loc){
        spawnPosition = new SVector(loc.toVector());
        spawnDirection = new SVector(loc.getDirection());
    }
    public Location getSpawn(World world){
        Location loc = spawnPosition.toVector().toLocation(world);
        loc.setDirection(spawnDirection.toVector());
        return loc;
    }

    /*public void setBedLocation(Location loc){
        bedLocation = loc;
    }
    public Location getBedLocation(){
        return bedLocation;
    }*/
    public boolean hasBed(){
        //return world.getBlockAt(bedLocation).getType().equals(color.getBed());
        return hasBed;
    }
    public void destroyBed(){
        hasBed = false;
    }
    public TeamColor color(){
        return color;
    }


}
package samann.bwplugin.bedwars;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.Serializable;


public class Spawner implements Serializable {
    private final SVector position;
    private final int cooldownTime;
    private int cooldown = 0;

    public Spawner(Location location) {
        this(location, 40);
    }
    public Spawner(Location location, int cooldownTime) {
        this.position = new SVector(location.toVector());
        this.cooldownTime = cooldownTime;
    }

    public Vector getPosition() {
        return position.toVector();
    }

    public void reset(){
        cooldown = cooldownTime;
    }

    public void onTick(World world, int multiplier) {
        cooldown -= multiplier;
        if(cooldown <= 0){
            cooldown = cooldownTime;

            //spawn goldnugget
            Location loc = position.toVector().toLocation(world);
            loc.add(0, 0.2, 0);
            Item item = world.dropItem(loc, new ItemStack(Bedwars.spawningMaterial, 1));
            item.setVelocity(new Vector(0, 0.1, 0));
        }
    }


}

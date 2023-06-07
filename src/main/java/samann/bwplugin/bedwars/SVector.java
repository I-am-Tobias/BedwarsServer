package samann.bwplugin.bedwars;

import org.bukkit.util.Vector;

import java.io.Serializable;

class SVector implements Serializable {
    public double x, y, z;

    public SVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SVector(Vector v) {
        this.x = v.getX();
        this.y = v.getY();
        this.z = v.getZ();
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
}

package samann.bwplugin.bedwars;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum TeamColor {
    RED,
    BLUE,
    GREEN,
    YELLOW;

    public String colorCode(){
        switch(this){
            case RED:
                return "§c";
            case BLUE:
                return "§9";
            case GREEN:
                return "§a";
            case YELLOW:
                return "§e";
        }
        return "";
    }
    public static TeamColor parse(Material bed) throws IllegalArgumentException{
        return TeamColor.valueOf(bed.name().split("_")[0]);
    }
    public String displayName(boolean colored){
        String name = colored ? colorCode() : "";
        switch(this){
            case RED:
                name += "Rot";
                break;
            case BLUE:
                name += "Blau";
                break;
            case GREEN:
                name += "Grün";
                break;
            case YELLOW:
                name += "Gelb";
                break;
        }
        return name;
    }
    public ChatColor chatColor(){
        switch(this){
            case RED:
                return ChatColor.RED;
            case BLUE:
                return ChatColor.BLUE;
            case GREEN:
                return ChatColor.GREEN;
            case YELLOW:
                return ChatColor.YELLOW;
        }
        return ChatColor.WHITE;
    }
    public String displayName(){
        return displayName(false);
    }
}
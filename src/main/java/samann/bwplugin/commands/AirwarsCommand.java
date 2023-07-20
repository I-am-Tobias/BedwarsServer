package samann.bwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.Airwars;
import samann.bwplugin.airwars.AirwarsPlayer;

import java.util.List;

public class AirwarsCommand  implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    Airwars airwars = new Airwars(((Player)sender).getWorld());
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      airwars.addPlayer(new AirwarsPlayer(player, airwars));
    }
    airwars.start();
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return null;
  }
}

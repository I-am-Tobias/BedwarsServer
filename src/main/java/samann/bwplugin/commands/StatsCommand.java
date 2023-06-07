package samann.bwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.bedwars.stats.BedwarsStats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsCommand implements CommandExecutor, TabCompleter {
    Argument<UUID> statsPlayerArgument = new Argument<UUID>("player", (String s) -> {
        OfflinePlayer p = Bukkit.getOfflinePlayer(s);
        return p.getUniqueId();
    }, () -> {
        List<String> players = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()){
            players.add(p.getName());
        }
        for(UUID u : BedwarsStats.allPlayers()){
            OfflinePlayer p = Bukkit.getOfflinePlayer(u);
            players.add(p.getName());
        }
        players.add("-top10");
        return players;
    });

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length == 0){
            Player p = (Player) sender;
            p.sendMessage("§2Du hast §a" + BedwarsStats.getStats(p) + " §2Punkte §7(Rang §6" + BedwarsStats.getRank(p) + "§7)");
        }else{
            if(args[0].equals("-top10")){
                List<OfflinePlayer> top10 = BedwarsStats.getTopPlayers(10);
                sender.sendMessage("§6§lTop 10: ");
                for(int i = 0; i < top10.size(); i++){
                    OfflinePlayer p = top10.get(i);
                    sender.sendMessage("§2" + (i + 1) + ". §3" + p.getName() + " §2- §a" + BedwarsStats.getStats(p));
                }
            }else{
                UUID uuid = statsPlayerArgument.parse(args[0], sender);
                sender.sendMessage("§3" + Bukkit.getOfflinePlayer(uuid).getName() + " §2hat §a" + BedwarsStats.getStats(uuid) + " §2Punkte §7(Rang §6" + BedwarsStats.getRank(uuid) + "§7)");
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = statsPlayerArgument.autocompletion(args[args.length - 1], sender);
        return list;
    }
}

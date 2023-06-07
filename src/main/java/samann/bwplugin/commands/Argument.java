package samann.bwplugin.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public class Argument<T> {
    private final String name;
    private final ArgumentParser<T> argumentParser;
    private final Proposals proposals;

    public Argument(String name, ArgumentParser<T> argumentParser, Proposals proposals) {
        this.name = name;
        this.argumentParser = argumentParser;
        this.proposals = proposals;
    }
    public Argument(String name, SimpleArgumentParser<T> argumentParser, SimpleProposals proposals) {
        this(name, (s, cs) -> argumentParser.parse(s), (cs) -> proposals.getProposals());
    }


    public String getName() {
        return name;
    }
    public List<String> autocompletion(String arg, CommandSender cs) {
        List<String> list = proposals.getProposals(cs);
        list.removeIf(s -> !s.toLowerCase().startsWith(arg.toLowerCase()));
        return list;
    }
    public T parse(String arg, CommandSender sender){
        return argumentParser.parse(arg, sender);
    }
    public boolean isValid(String arg, CommandSender sender){
        return argumentParser.parse(arg, sender) != null;
    }



    public interface ArgumentParser<T>{
        T parse(String arg, CommandSender sender);
    }
    public interface SimpleArgumentParser<T>{
        T parse(String arg);
    }
    public interface Proposals {
        List<String> getProposals(CommandSender sender);
    }
    public interface SimpleProposals{
        List<String> getProposals();
    }
}

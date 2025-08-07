package fr.Nat0uille.NATLobby.TabCompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class NatLobbyTabCompleter implements TabCompleter {
    // This class can be used to provide tab completion for commands in the NATLobby plugin.
    // Currently, it does not implement any methods, but you can add logic to handle tab completion.

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return List.of("reload");
        }

        return Collections.emptyList();
    }
}

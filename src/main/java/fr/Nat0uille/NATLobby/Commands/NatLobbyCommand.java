package fr.Nat0uille.NATLobby.Commands;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NatLobbyCommand implements CommandExecutor {

    private final Main main;
    MiniMessage mm = MiniMessage.miniMessage();

    public NatLobbyCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.NatLobby.Help"))));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("natlobby.reload")) {
                sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.NoPermission"))));
                return true;
            }
            main.reloadConfig();
            sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.NatLobby.Reload"))));
            return true;
        }
        return false;
    }


}

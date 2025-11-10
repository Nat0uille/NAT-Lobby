package fr.Nat0uille.NATLobby.Commands;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {

    private final Main main;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BuildCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(main.getConfig().getString("Commands.BuildConsole")));
            return true;
        }
        if (!sender.hasPermission("natlobby.build")) {
            sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.NoPermission"))));
            return true;
        }
        toggleBuild(player);
        return true;
    }

    private void toggleBuild(Player player) {
        java.util.UUID uuid = player.getUniqueId();
        boolean current = main.isBuild(uuid);
        boolean now = !current;
        main.setBuild(uuid, now);

        if (now) {
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.BuildEnabled"))));
        } else {
            player.setGameMode(GameMode.ADVENTURE);
            main.getPlayerListener().applyLobbyItems(player);
            player.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.BuildDisabled"))));
        }
    }
}

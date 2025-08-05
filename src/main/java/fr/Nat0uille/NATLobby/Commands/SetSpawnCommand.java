package fr.Nat0uille.NATLobby.Commands;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private final Main main;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SetSpawnCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Component prefix = mm.deserialize(main.getConfig().getString("Prefix"));
        Component SetSpawn = mm.deserialize(main.getConfig().getString("Commands.SetSpawn"));
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.SetSpawnConsole"))));
            return true;
        }
        if (!player.hasPermission("natlobby.setspawn")) {
            player.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.NoPermission"))));
            return true;
        }

        Location loc = player.getLocation();
        main.getConfig().set("OnJoin.Teleport.World", loc.getWorld().getName());
        main.getConfig().set("OnJoin.Teleport.X", loc.getX());
        main.getConfig().set("OnJoin.Teleport.Y", loc.getY());
        main.getConfig().set("OnJoin.Teleport.Z", loc.getZ());
        main.getConfig().set("OnJoin.Teleport.Yaw", loc.getYaw());
        main.getConfig().set("OnJoin.Teleport.Pitch", loc.getPitch());
        main.saveConfig();

        player.sendMessage(prefix.append(SetSpawn));
        return true;
    }
}
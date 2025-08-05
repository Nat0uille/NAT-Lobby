package fr.Nat0uille.NATLobby.Commands;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class SpawnCommand implements CommandExecutor {

    private final Main main;
    MiniMessage mm = MiniMessage.miniMessage();

    public SpawnCommand(Main main) {
        this.main = main;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Component prefix = mm.deserialize(main.getConfig().getString("Prefix"));
        ConfigurationSection tp = main.getConfig().getConfigurationSection("OnJoin.Teleport");
        if (tp != null) {
            String worldName = tp.getString("World");
            double x = tp.getDouble("X");
            double y = tp.getDouble("Y");
            double z = tp.getDouble("Z");
            float yaw = (float) tp.getDouble("Yaw");
            float pitch = (float) tp.getDouble("Pitch");
            World world = Bukkit.getWorld(worldName);
            Location loc = null;
            if (world != null) {
                loc = new Location(world, x, y, z, yaw, pitch);
            }
            if (args.length == 0) {
                if (sender instanceof org.bukkit.entity.Player player) {
                    if (loc != null) {
                        player.teleport(loc);
                        player.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.Spawn"))));
                    }
                } else {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.SpawnConsole"))));
                }
            }
            if (args.length == 1) {
                if (Bukkit.getPlayer(args[0]) != null) {
                    org.bukkit.entity.Player target = Bukkit.getPlayer(args[0]);
                    if (loc != null) {
                        target.teleport(loc);
                        target.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.Spawn"))));
                        sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.SpawnTargetSender")
                                .replace("{target}", target.getName()))));
                    }
                } else {
                    sender.sendMessage(prefix.append(mm.deserialize(main.getConfig().getString("Commands.SpawnTargetNotFound")
                    .replace("{target}", args[0]))));
                }
            }
        }
        return true;
    }
}

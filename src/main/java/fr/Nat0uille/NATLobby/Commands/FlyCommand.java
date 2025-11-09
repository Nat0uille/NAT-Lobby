package fr.Nat0uille.NATLobby.Commands;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class FlyCommand implements CommandExecutor {

    private final Main main;
    MiniMessage mm = MiniMessage.miniMessage();

    public FlyCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("natlobby.fly")) {
            sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.NoPermission"))));
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player player) {
                boolean isFlying = player.getAllowFlight();
                player.setAllowFlight(!isFlying);
                saveFlyState(player.getUniqueId().toString(), !isFlying);
                if (isFlying) {
                    player.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyDisabled"))));
                } else {
                    player.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyEnabled"))));
                }
            } else {
                sender.sendMessage(mm.deserialize(main.getConfig().getString("Commands.FlyConsole")));
            }
        }

        if (args.length == 1) {
            if (!sender.hasPermission("natlobby.fly.others")) {
                sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.NoPermission"))));
                return true;
            }
            if (Bukkit.getPlayer(args[0]) != null) {
                Player target = Bukkit.getPlayer(args[0]);
                boolean isFlying = target.getAllowFlight();
                target.setAllowFlight(!isFlying);
                saveFlyState(target.getUniqueId().toString(), !isFlying);
                if (isFlying) {
                    target.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyDisabled"))));
                } else {
                    target.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyEnabled"))));
                }
                sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyTargetSender")
                        .replace("{target}", target.getName()))));
            } else {
                sender.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyTargetNotFound")
                        .replace("{target}", args[0]))));
            }
        }
        return true;
    }


    private void saveFlyState(String uuid, boolean enabled) {
        FileConfiguration stats = main.getStats();
        stats.set("players." + uuid + ".fly", enabled);
        main.saveStats();
    }

}
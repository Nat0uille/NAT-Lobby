package fr.Nat0uille.NATLobby.Listeners;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class PlayerListener implements Listener {

    private final Main main;
    MiniMessage mm = MiniMessage.miniMessage();
    private final Map<java.util.UUID, Map<Integer, List<String>>> itemCommands = new HashMap<>();

    public PlayerListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        event.getPlayer().getInventory().clear();
        event.getPlayer().getInventory().setArmorContents(null);
        event.getPlayer().setHealth(20.0);
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setExp(0);
        event.getPlayer().setLevel(0);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);

        FileConfiguration stats = main.getStats();
        String uuid = event.getPlayer().getUniqueId().toString();
        boolean fly = stats.getBoolean("players." + uuid + ".fly", false);
        Player player = event.getPlayer();
        player.setAllowFlight(fly);
        if (fly) {
            player.setFlying(true);
            player.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.FlyEnabled"))));
        }

        boolean build = main.isBuild(player.getUniqueId());
        if (build) {
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(mm.deserialize(main.getConfig().getString("Prefix")).append(mm.deserialize(main.getConfig().getString("Commands.BuildEnabled"))));
        }

        Component joinMessage = mm.deserialize(
                main.getConfig().getString("OnJoin.Message")
                        .replace("{prefix}", main.getConfig().getString("Prefix"))
                        .replace("{player}", event.getPlayer().getName())
                        .replace("{online-players}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{max-players}", String.valueOf(Bukkit.getMaxPlayers()))
        );
        if (!(main.getConfig().getString("OnJoin.Message") == "none")) {
        Bukkit.broadcast(joinMessage);}

        ConfigurationSection tp = main.getConfig().getConfigurationSection("OnJoin.Teleport");
        if (tp != null) {
            String worldName = tp.getString("World");
            double x = tp.getDouble("X");
            double y = tp.getDouble("Y");
            double z = tp.getDouble("Z");
            float yaw = (float) tp.getDouble("Yaw");
            float pitch = (float) tp.getDouble("Pitch");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location loc = new Location(world, x, y, z, yaw, pitch);
                event.getPlayer().teleport(loc);
            }
        }

        applyLobbyItems(player);
    }

    public void applyLobbyItems(Player player) {
        itemCommands.remove(player.getUniqueId());
        Map<Integer, List<String>> commandsForPlayer = new HashMap<>();

        ConfigurationSection itemsSection = main.getConfig().getConfigurationSection("OnJoin.Items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                if (commandsForPlayer.size() >= 9) break;
                ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
                if (itemSec == null) continue;
                String materialName = itemSec.getString("Material");
                String name = itemSec.getString("Name");
                List<String> lore = itemSec.getStringList("Lore");
                int slot = itemSec.getInt("Slot");
                List<String> Commands = itemSec.getStringList("Commands");
                ItemStack item = null;

                if (materialName != null && Bukkit.getPluginManager().isPluginEnabled("ItemsAdder") && materialName.contains(":")) {
                    try {
                        Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
                        Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, materialName);
                        if (customStack != null) {
                            item = (ItemStack) customStackClass.getMethod("getItemStack").invoke(customStack);
                        }
                    } catch (Exception ignored) { }
                }

                if (item == null) {
                    Material material = Material.matchMaterial(materialName);
                    if (material == null) continue;
                    item = new ItemStack(material);
                }

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (name != null) meta.displayName(mm.deserialize("<yellow><!i>" + name));
                    if (lore != null && !lore.isEmpty()) meta.lore(lore.stream().map(mm::deserialize).toList());
                    NamespacedKey keyMenu = new NamespacedKey("natlobby", "item_" + slot);
                    meta.getPersistentDataContainer().set(keyMenu, PersistentDataType.BYTE, (byte) 1);
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey("natlobby", "item_slot_" + slot),
                            PersistentDataType.INTEGER, slot
                    );
                    item.setItemMeta(meta);
                }
                player.getInventory().setItem(slot, item);
                commandsForPlayer.put(slot, Commands);
            }
        }

        itemCommands.put(player.getUniqueId(), commandsForPlayer);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FileConfiguration stats = main.getStats();
        String uuid = player.getUniqueId().toString();
        stats.set("players." + uuid + ".fly", player.getAllowFlight());
        itemCommands.remove(player.getUniqueId());

        event.setQuitMessage(null);
        Component quitMessage = mm.deserialize(
                main.getConfig().getString("OnQuit")
                        .replace("{prefix}", main.getConfig().getString("Prefix"))
                        .replace("{player}", event.getPlayer().getName())
                        .replace("{online-players}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{max-players}", String.valueOf(Bukkit.getMaxPlayers()))
        );
        if (!(main.getConfig().getString("OnQuit") == "none")) {
        Bukkit.broadcast(quitMessage);}
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            Player player = (Player) event.getWhoClicked();
            boolean build = main.isBuild(player.getUniqueId());
            if (!build) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            Player player = (Player) event.getWhoClicked();
            boolean build = main.isBuild(player.getUniqueId());
            if (!build) event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        boolean build = main.isBuild(player.getUniqueId());
        if (!build) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        boolean build = main.isBuild(player.getUniqueId());
        if (!build) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        Map<Integer, List<String>> commandsForPlayer = itemCommands.get(player.getUniqueId());
        if (commandsForPlayer == null) return;

        for (int i = 0; i < 9; i++) {
            NamespacedKey keySlot = new NamespacedKey("natlobby", "item_slot_" + i);
            Integer slot = meta.getPersistentDataContainer().get(keySlot, PersistentDataType.INTEGER);
            if (slot != null) {
                NamespacedKey keyMenu = new NamespacedKey("natlobby", "item_" + slot);
                if (!meta.getPersistentDataContainer().has(keyMenu, PersistentDataType.BYTE)) continue;

                List<String> commands = commandsForPlayer.get(slot);
                if (commands != null) {
                    for (String command : commands) {
                        if (command.startsWith("[PLAYER] ")) {
                            player.performCommand(command.substring(9));
                        } else if (command.startsWith("[CONSOLE] ")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring(10).replace("{player}", player.getName()));
                        } else if (command.startsWith("[WORLD] ")) {
                            String[] args = command.substring(8).split(" ");
                            if (args.length >= 4) {
                                World world = Bukkit.getWorld(args[0]);
                                double x = Double.parseDouble(args[1]);
                                double y = Double.parseDouble(args[2]);
                                double z = Double.parseDouble(args[3]);
                                if (world != null) player.teleport(new Location(world, x, y, z));
                            }
                        }
                    }
                }
            }
        }
    }
}

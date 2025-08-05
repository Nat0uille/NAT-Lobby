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

import java.util.*;

public class PlayerListener implements Listener {

    private final Main main;
    MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, List<String>> itemCommands = new HashMap<>();

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

        // Message de join
        Component joinMessage = mm.deserialize(
                main.getConfig().getString("OnJoin.Message")
                        .replace("{prefix}", main.getConfig().getString("Prefix"))
                        .replace("{player}", event.getPlayer().getName())
                        .replace("{online-players}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{max-players}", String.valueOf(Bukkit.getMaxPlayers()))
        );
        Bukkit.broadcast(joinMessage);

        // Téléportation
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

        // Items personnalisés
        itemCommands.clear();
        ConfigurationSection itemsSection = main.getConfig().getConfigurationSection("OnJoin.Items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                if (itemCommands.size() >= 9) break;
                ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
                if (itemSec == null) continue;
                String materialName = itemSec.getString("Material");
                String name = itemSec.getString("Name");
                List<String> lore = itemSec.getStringList("Lore");
                int slot = itemSec.getInt("Slot");
                List<String> Commands = itemSec.getStringList("Commands");
                Material material = Material.matchMaterial(materialName);
                if (material == null) continue;
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(mm.deserialize("<yellow><!i>" + name));
                    meta.lore(lore.stream().map(mm::deserialize).toList());
                    NamespacedKey keyMenu = new NamespacedKey("natlobby", "item_" + slot);
                    meta.getPersistentDataContainer().set(keyMenu, PersistentDataType.BYTE, (byte) 1);
                    meta.getPersistentDataContainer().set(
                        new NamespacedKey("natlobby", "item_slot_" + slot),
                        PersistentDataType.INTEGER, slot
                    );
                    item.setItemMeta(meta);
                }
                event.getPlayer().getInventory().setItem(slot, item);
                itemCommands.put(slot, Commands);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Component quitMessage = mm.deserialize(
                main.getConfig().getString("onquit")
                        .replace("{pseudo}", event.getPlayer().getName())
                        .replace("{prefix}", main.getConfig().getString("Prefix"))
                        .replace("{joueur}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{joueurmax}", String.valueOf(Bukkit.getMaxPlayers()))
        );
        Bukkit.broadcast(quitMessage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
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

        for (int i = 0; i < 9; i++) {
            NamespacedKey keySlot = new NamespacedKey("natlobby", "item_slot_" + i);
            Integer slot = meta.getPersistentDataContainer().get(keySlot, PersistentDataType.INTEGER);
            if (slot != null) {
                NamespacedKey keyMenu = new NamespacedKey("natlobby", "item_" + slot);
                if (!meta.getPersistentDataContainer().has(keyMenu, PersistentDataType.BYTE)) continue;

                List<String> commands = itemCommands.get(slot);
                if (commands == null) return;

                event.setCancelled(true);
                for (String cmd : commands) {
                    if (cmd.startsWith("[PLAYER] ")) {
                        player.performCommand(cmd.substring(9));
                    } else if (cmd.startsWith("[CONSOLE] ")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(10).replace("{player}", player.getName()));
                    } else if (cmd.startsWith("[WORLD] ")) {
                        String[] args = cmd.substring(8).split(" ");
                        if (args.length >= 4) {
                            World world = Bukkit.getWorld(args[0]);
                            double x = Double.parseDouble(args[1]);
                            double y = Double.parseDouble(args[2]);
                            double z = Double.parseDouble(args[3]);
                            if (world != null) player.teleport(new Location(world, x, y, z));
                        }
                    }
                }
                return;
            }
        }
    }
}
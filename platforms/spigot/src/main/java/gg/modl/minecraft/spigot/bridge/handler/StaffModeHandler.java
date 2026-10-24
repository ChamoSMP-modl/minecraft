package gg.modl.minecraft.spigot.bridge.handler;

import gg.modl.minecraft.bridge.BridgeScheduler;
import gg.modl.minecraft.bridge.config.BridgeConfig;
import gg.modl.minecraft.bridge.config.StaffModeConfig;
import gg.modl.minecraft.bridge.locale.BridgeLocaleManager;
import gg.modl.minecraft.bridge.query.BridgeQueryClient;
import gg.modl.minecraft.bridge.staffmode.StaffModeCore;
import gg.modl.minecraft.core.util.PluginLogger;
import gg.modl.minecraft.spigot.bridge.folia.AsyncTeleporter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class StaffModeHandler implements Listener {
    static final String SILENT_CONTAINER_PREFIX = "<dark_gray>Viewing: ";

    private final JavaPlugin plugin;
    private final StaffModeCore core;

    public StaffModeHandler(JavaPlugin plugin, BridgeConfig bridgeConfig, FreezeHandler freezeHandler,
                            BridgeLocaleManager localeManager, PluginLogger pluginLogger,
                            StaffModeConfig staffModeConfig, BridgeScheduler scheduler) {
        this.plugin = plugin;

        this.core = new StaffModeCore(
                bridgeConfig,
                staffModeConfig,
                localeManager,
                pluginLogger,
                scheduler,
                freezeHandler.getFreezeCore(),
                new SpigotStaffModeOps(
                        plugin.getLogger(),
                        new AsyncTeleporter(pluginLogger),
                        plugin
                )
        );
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        core.start();
    }

    public void shutdown() {
        core.shutdown();
    }

    public void setBridgeClient(BridgeQueryClient bridgeClient) {
        core.setBridgeClient(bridgeClient);
    }

    public boolean isInStaffMode(UUID uuid) {
        return core.isInStaffMode(uuid);
    }

    public void enterStaffMode(String staffUuid) {
        core.enterStaffMode(staffUuid);
    }

    public void exitStaffMode(String staffUuid) {
        core.exitStaffMode(staffUuid);
    }

    public void setTarget(String staffUuid, String targetUuid) {
        core.setTarget(staffUuid, targetUuid);
    }

    public void vanishFromBridge(String staffUuid) {
        core.vanishFromBridge(staffUuid);
    }

    public void unvanishFromBridge(String staffUuid) {
        core.unvanishFromBridge(staffUuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getAction() == Action.PHYSICAL) {
            if (core.isVanished(uuid)) {
                event.setCancelled(true);
            }
            return;
        }

        if (!core.isInStaffMode(uuid)) return;

        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                core.handleHotbarAction(uuid, player.getInventory().getHeldItemSlot());
            }
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && core.isVanished(uuid)) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof InventoryHolder holder) {
                event.setCancelled(true);
                Inventory copy = Bukkit.createInventory(
                        null,
                        holder.getInventory().getSize(),
                        MiniMessage.miniMessage().deserialize(SILENT_CONTAINER_PREFIX + block.getType().name())
                );
                copy.setContents(holder.getInventory().getContents());
                player.openInventory(copy);
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!core.isInStaffMode(uuid)) return;
        event.setCancelled(true);

        if (event.getRightClicked() instanceof Player clicked) {
            core.handleTargetSelect(uuid, player.getInventory().getHeldItemSlot(), clicked.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        cancelIfInStaffMode(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        cancelIfInStaffMode(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (core.isInStaffMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageOther(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (core.isInStaffMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && core.isInStaffMode(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        cancelIfInStaffMode(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (core.isInStaffMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!core.isInStaffMode(player.getUniqueId())) return;

        if (MiniMessage.miniMessage().serialize(event.getView().title()).startsWith(SILENT_CONTAINER_PREFIX)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (core.isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        core.handlePlayerJoin(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        core.handlePlayerQuit(event.getPlayer().getUniqueId());
    }

    private void cancelIfInStaffMode(Player player, Cancellable event) {
        if (core.isInStaffMode(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

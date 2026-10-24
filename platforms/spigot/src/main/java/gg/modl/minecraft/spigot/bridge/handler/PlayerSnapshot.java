package gg.modl.minecraft.spigot.bridge.handler;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

record PlayerSnapshot(ItemStack[] inventoryContents, ItemStack[] armorContents, Location location, GameMode gameMode,
                      double health, int foodLevel, float exp, int level) {
}

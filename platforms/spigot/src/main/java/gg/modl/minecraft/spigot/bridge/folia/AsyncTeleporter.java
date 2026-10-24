package gg.modl.minecraft.spigot.bridge.folia;

import gg.modl.minecraft.core.util.PluginLogger;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class AsyncTeleporter {
    private final PluginLogger logger;

    public AsyncTeleporter(PluginLogger logger) {
        this.logger = logger;
    }

    public void teleport(Entity entity, Location destination) {
        entity.teleportAsync(destination)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        logger.warning("[Bridge] Failed to teleport " + entity.getName(), failure);
                    } else if (Boolean.FALSE.equals(result)) {
                        logger.warning("[Bridge] Teleport of " + entity.getName() + " was refused");
                    }
                });
    }
}
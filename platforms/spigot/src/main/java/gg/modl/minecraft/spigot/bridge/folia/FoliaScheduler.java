package gg.modl.minecraft.spigot.bridge.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class FoliaScheduler {

    private final JavaPlugin plugin;

    public FoliaScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void runGlobal(Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, _ -> task.run());
    }

    public void runForEntity(Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, _ -> task.run(), null);
    }

    public void runForEntityLater(Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, _ -> task.run(), null, delayTicks);
    }

    public ScheduledTask runAsyncTimer(Runnable task, long delay, long period, TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, _ -> task.run(), delay, period, unit);
    }

    public void cancelFoliaTask(ScheduledTask scheduledTask) {
        scheduledTask.cancel();
    }
}
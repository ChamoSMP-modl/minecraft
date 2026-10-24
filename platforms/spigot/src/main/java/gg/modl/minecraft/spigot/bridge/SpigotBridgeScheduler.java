package gg.modl.minecraft.spigot.bridge;

import gg.modl.minecraft.bridge.BridgeScheduler;
import gg.modl.minecraft.bridge.BridgeTask;
import gg.modl.minecraft.spigot.bridge.folia.FoliaScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpigotBridgeScheduler implements BridgeScheduler {
    @Getter
    private final JavaPlugin plugin;
    @Getter
    private final FoliaScheduler foliaScheduler;
    private final ScheduledExecutorService delayExecutor;

    public SpigotBridgeScheduler(JavaPlugin plugin) {
        this(plugin, null);
    }

    SpigotBridgeScheduler(JavaPlugin plugin, ScheduledExecutorService delayExecutor) {
        this.plugin = plugin;
        this.foliaScheduler = new FoliaScheduler(plugin);
        this.delayExecutor = delayExecutor != null ? delayExecutor : createDelayExecutor();
    }

    private static ScheduledExecutorService createDelayExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "modl-bridge-delay");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void runOnMainThread(Runnable task) {
        foliaScheduler.runGlobal(task);
    }

    @Override
    public void runForPlayer(UUID playerUuid, Runnable task) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            foliaScheduler.runForEntity(player, task);
        }
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        long delayMs = delayTicks * 50L;
        delayExecutor.schedule(() -> foliaScheduler.runGlobal(task),
                delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runForPlayerLater(UUID playerUuid, Runnable task, long delayTicks) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            foliaScheduler.runForEntityLater(player, task, delayTicks);
        }
    }

    @Override
    public BridgeTask runTimerAsync(Runnable task, long delay, long period, TimeUnit unit) {
        return () -> foliaScheduler.cancelFoliaTask(
                foliaScheduler.runAsyncTimer(task, delay, period, unit)
        );
    }

    @Override
    public void cancelTask(BridgeTask task) {
        if (task != null) task.cancel();
    }

    public void shutdown() {
        if (delayExecutor != null) {
            delayExecutor.shutdownNow();
        }
    }
}
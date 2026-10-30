package gg.modl.minecraft.spigot;

import gg.modl.minecraft.bridge.config.BridgeWizardConfigWriter;
import gg.modl.minecraft.core.boot.BootConfig;
import gg.modl.minecraft.core.boot.ConsoleInput;
import gg.modl.minecraft.core.boot.PlatformType;
import gg.modl.minecraft.core.boot.SetupWizard;
import gg.modl.minecraft.core.util.PluginLogger;
import gg.modl.minecraft.spigot.bridge.folia.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SpigotSetupWizard implements Listener {
    private static final long WIZARD_START_DELAY_SECONDS = 3L;

    private final JavaPlugin plugin;
    private final PluginLogger logger;
    private final Consumer<BootConfig> onComplete;
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private final FoliaScheduler foliaScheduler;
    private volatile boolean active = false;

    public SpigotSetupWizard(JavaPlugin plugin, PluginLogger logger, Consumer<BootConfig> onComplete) {
        this.plugin = plugin;
        this.logger = logger;
        this.onComplete = onComplete;
        this.foliaScheduler = new FoliaScheduler(plugin);
    }

    public void start() {
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        startFolia();
    }

    private void startFolia() {
        try (
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "modl-setup-wizard");
                    t.setDaemon(true);
                    return t;
                })
        ) {
            executor.schedule(this::runWizard, WIZARD_START_DELAY_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
        }
    }

    private void runWizard() {
        try {
            ConsoleInput input = new ServerCommandInput();
            SetupWizard wizard = new SetupWizard(logger, input, PlatformType.PAPER);
            BootConfig config = wizard.run();

            if (config != null) {
                config.save(plugin.getDataFolder().toPath());
                BridgeWizardConfigWriter.writeBridgeOnlyConfig(plugin.getDataFolder().toPath(), config, logger);
                logger.info("Configuration saved to boot.yml.");
                logger.info("Initializing plugin...");
                runOnMain(() -> onComplete.accept(config));
            } else {
                fallbackToTemplate();
            }
        } catch (Exception e) {
            logger.severe("Setup wizard error: " + e.getMessage());
            fallbackToTemplate();
        } finally {
            active = false;
            runOnMain(() -> HandlerList.unregisterAll(this));
        }
    }

    private void runOnMain(Runnable task) {
        foliaScheduler.runGlobal(task);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        if (!active) return;
        event.setCancelled(true);
        inputQueue.offer(event.getCommand());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!active) return;
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                Component.text("Server is running first-time setup. Please try again shortly."));
    }

    private void fallbackToTemplate() {
        try {
            BootConfig.saveTemplate(plugin.getDataFolder().toPath());
        } catch (IOException ignored) {
        }
        logger.warning("Setup cancelled. A boot.yml template has been created.");
        logger.info("Edit plugins/" + plugin.getName() + "/boot.yml and restart.");
    }

    private class ServerCommandInput implements ConsoleInput {
        @Override
        public String readLine(String prompt) {
            logger.info(prompt);
            try {
                return inputQueue.take();
            } catch (InterruptedException e) {
                return null;
            }
        }
    }
}

package gg.modl.minecraft.bridge;

import java.nio.file.Path;

public interface BridgePluginContext {

    BridgeScheduler getScheduler();

    BridgePlayerProvider getPlayerProvider();

    Path dataFolder();

    void saveDefaultResource(String resourcePath);

    String getMinecraftVersion();
}

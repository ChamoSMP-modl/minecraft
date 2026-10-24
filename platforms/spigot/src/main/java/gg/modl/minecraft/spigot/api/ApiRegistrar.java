package gg.modl.minecraft.spigot.api;

import gg.modl.minecraft.api.http.*;
import gg.modl.minecraft.core.HttpManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public class ApiRegistrar {
    private final Plugin plugin;
    private final HttpManager httpManager;

    public ApiRegistrar(Plugin plugin, HttpManager httpManager) {
        this.plugin = plugin;
        this.httpManager = httpManager;
    }

    public void register() {
        ServicesManager sm = Bukkit.getServicesManager();
        ModlHttpClient client = httpManager.getHttpClient();

        sm.register(TicketApi.class, client, plugin, ServicePriority.Normal);
        sm.register(SyncApi.class, client, plugin, ServicePriority.Normal);
        sm.register(StaffApi.class, client, plugin, ServicePriority.Normal);
        sm.register(ReportApi.class, client, plugin, ServicePriority.Normal);
        sm.register(PunishmentApi.class, client, plugin, ServicePriority.Normal);
        sm.register(PlayerApi.class, client, plugin, ServicePriority.Normal);
        sm.register(MigrationApi.class, client, plugin, ServicePriority.Normal);
        sm.register(LogApi.class, client, plugin, ServicePriority.Normal);
    }
}

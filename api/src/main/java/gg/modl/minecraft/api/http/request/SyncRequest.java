package gg.modl.minecraft.api.http.request;

import gg.modl.minecraft.api.http.ChatLogEntry;
import gg.modl.minecraft.api.http.CommandLogEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SyncRequest(@NotNull String lastSyncTimestamp, @NotNull List<OnlinePlayer> onlinePlayers,
                          @Nullable String serverName, @Nullable String serverInstanceId,
                          @Nullable List<ChatLogEntry> chatLogs, @Nullable List<CommandLogEntry> commandLogs,
                          @Nullable ServerStatus serverStatus) {
    public record ServerStatus(int onlinePlayerCount, int maxPlayers, @Nullable String serverVersion,
                               @Nullable String platformType, @Nullable String pluginVersion, long timestamp) {
    }

    public record OnlinePlayer(@NotNull String uuid, @NotNull String username, @NotNull String ipAddress,
                               long sessionDurationMs) {
    }
}

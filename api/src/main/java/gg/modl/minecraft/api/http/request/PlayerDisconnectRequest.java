package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayerDisconnectRequest(@NotNull String minecraftUuid, long sessionDurationMs,
                                      @Nullable String serverInstanceId) {
}

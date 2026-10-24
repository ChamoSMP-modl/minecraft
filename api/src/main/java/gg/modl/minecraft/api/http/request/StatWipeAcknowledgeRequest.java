package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StatWipeAcknowledgeRequest(@NotNull String punishmentId, @Nullable String serverName, boolean success) {
}

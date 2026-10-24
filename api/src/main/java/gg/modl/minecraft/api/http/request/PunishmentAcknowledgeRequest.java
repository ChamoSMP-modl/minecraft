package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PunishmentAcknowledgeRequest(@NotNull String punishmentId, @NotNull String playerUuid,
                                           @NotNull String executedAt, @Nullable String errorMessage, boolean success) {
}

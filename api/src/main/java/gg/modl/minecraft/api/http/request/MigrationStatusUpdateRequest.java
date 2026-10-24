package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MigrationStatusUpdateRequest(@NotNull String taskId, @NotNull String status, @NotNull String message,
                                           @Nullable Integer recordsProcessed, @Nullable Integer totalRecords) {
}

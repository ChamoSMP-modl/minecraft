package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CreatePlayerNoteRequest(@NotNull String targetUuid, @Nullable String issuerName,
                                      @Nullable String issuerId, @NotNull String text) {
}

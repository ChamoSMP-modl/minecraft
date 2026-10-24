package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PardonPunishmentRequest(@NotNull String punishmentId, @Nullable String issuerName,
                                      @Nullable String issuerId, @Nullable String reason,
                                      @Nullable String expectedType) {
}

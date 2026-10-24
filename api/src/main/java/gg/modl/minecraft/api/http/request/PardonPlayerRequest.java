package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PardonPlayerRequest(@NotNull String playerName, @Nullable String issuerName, @Nullable String issuerId,
                                  @Nullable String punishmentType, @Nullable String reason) {
}

package gg.modl.minecraft.api.http.request;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CreatePunishmentRequest(@NotNull String targetUuid, @Nullable String issuerName,
                                      @Nullable String issuerId, @Nullable String reason, @Nullable JsonObject data,
                                      @Nullable List<String> notes, @Nullable List<String> attachedTicketIds,
                                      @SerializedName("type_ordinal") int typeOrdinal, long duration) {
}

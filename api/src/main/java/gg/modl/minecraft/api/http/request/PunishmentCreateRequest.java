package gg.modl.minecraft.api.http.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public record PunishmentCreateRequest(String targetUuid, String issuerName, String issuerId, String reason,
                                      String severity, String status, @SerializedName("type_ordinal") int typeOrdinal,
                                      Long duration, Map<String, Object> data, List<String> notes,
                                      List<String> attachedTicketIds) {
}

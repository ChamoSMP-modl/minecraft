package gg.modl.minecraft.api.http.request;

public record AddPunishmentEvidenceRequest(String punishmentId, String issuerName, String issuerId,
                                           String evidenceUrl) {
}

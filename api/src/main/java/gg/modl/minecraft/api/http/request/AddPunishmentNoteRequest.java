package gg.modl.minecraft.api.http.request;

public record AddPunishmentNoteRequest(String punishmentId, String issuerName, String issuerId, String note) {
}

package gg.modl.minecraft.api.http.request;

public record ChangePunishmentDurationRequest(String punishmentId, String issuerName, String issuerId,
                                              Long newDuration) {
}

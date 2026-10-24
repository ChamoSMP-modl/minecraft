package gg.modl.minecraft.api.http.request;

public record TogglePunishmentOptionRequest(String punishmentId, String issuerName, String issuerId, String option,
                                            boolean enabled) {
}

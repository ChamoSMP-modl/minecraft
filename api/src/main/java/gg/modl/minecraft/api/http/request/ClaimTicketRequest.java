package gg.modl.minecraft.api.http.request;

public record ClaimTicketRequest(String ticketId, String playerUuid, String playerName) {
}

package gg.modl.minecraft.api.http.request;

import java.util.List;

public record ModifyPunishmentTicketsRequest(String punishmentId, String issuerName, String issuerId,
                                             List<String> addTicketIds, List<String> removeTicketIds,
                                             boolean modifyAssociatedTickets) {
}

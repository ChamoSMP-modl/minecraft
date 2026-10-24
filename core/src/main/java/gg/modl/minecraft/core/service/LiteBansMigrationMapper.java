package gg.modl.minecraft.core.service;

import gg.modl.minecraft.core.migration.StreamingJsonWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class LiteBansMigrationMapper {
    private LiteBansMigrationMapper() {
    }

    static StreamingJsonWriter.PlayerData toPlayerData(LiteBansMigrationRepository.PlayerRecord player) {
        return new StreamingJsonWriter.PlayerData(
                player.minecraftUuid(),
                convertUsernames(player.usernames()),
                Collections.emptyList(),
                convertIpList(player.ipList()),
                convertPunishments(player.punishments()),
                null
        );
    }

    private static List<StreamingJsonWriter.UsernameEntry> convertUsernames(List<LiteBansMigrationRepository.UsernameRecord> usernames) {
        List<StreamingJsonWriter.UsernameEntry> result = new ArrayList<>();
        for (LiteBansMigrationRepository.UsernameRecord username : usernames) {
            result.add(new StreamingJsonWriter.UsernameEntry(username.username(), username.date()));
        }
        return result;
    }

    private static List<StreamingJsonWriter.IpEntry> convertIpList(List<LiteBansMigrationRepository.IpRecord> ipList) {
        List<StreamingJsonWriter.IpEntry> result = new ArrayList<>();
        for (LiteBansMigrationRepository.IpRecord ip : ipList) {
            result.add(new StreamingJsonWriter.IpEntry(
                    ip.ipAddress(),
                    ip.country(),
                    ip.region(),
                    ip.asn(),
                    ip.proxy(),
                    ip.hosting(),
                    ip.firstLogin(),
                    new ArrayList<>(ip.logins())
            ));
        }
        return result;
    }

    private static List<StreamingJsonWriter.PunishmentEntry> convertPunishments(List<LiteBansMigrationRepository.PunishmentRecord> punishments) {
        List<StreamingJsonWriter.PunishmentEntry> result = new ArrayList<>();
        for (LiteBansMigrationRepository.PunishmentRecord p : punishments) {
            result.add(new StreamingJsonWriter.PunishmentEntry(
                    p.id(), p.type(), p.typeOrdinal(), p.reason(), p.issued(),
                    p.issuerName(), p.duration(), p.started(), p.data(),
                    p.notes(), p.evidence(), p.attachedTicketIds(), Collections.emptyList()
            ));
        }
        return result;
    }
}

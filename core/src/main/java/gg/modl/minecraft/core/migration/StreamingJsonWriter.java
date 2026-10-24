package gg.modl.minecraft.core.migration;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import gg.modl.minecraft.core.util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamingJsonWriter implements AutoCloseable {
    private final JsonWriter jsonWriter;
    private final FileWriter fileWriter;
    private final Gson gson;
    private final String defaultReason;
    private boolean closed = false;

    public StreamingJsonWriter(File outputFile, String defaultReason) throws IOException {
        this.fileWriter = new FileWriter(outputFile);
        this.jsonWriter = new JsonWriter(fileWriter);
        this.jsonWriter.setIndent("  ");
        this.gson = new Gson();
        this.defaultReason = defaultReason;
        try {
            jsonWriter.beginObject();
            jsonWriter.name("players");
            jsonWriter.beginArray();
        } catch (IOException e) {
            try {
                jsonWriter.close();
            } catch (IOException ignored) {}
            throw e;
        }
    }

    public void writePlayer(PlayerData playerData) throws IOException {
        if (closed) throw new IllegalStateException("Writer is closed");

        jsonWriter.beginObject();
        jsonWriter.name("minecraftUuid").value(playerData.minecraftUuid());

        jsonWriter.name("usernames");
        jsonWriter.beginArray();
        for (UsernameEntry username : playerData.usernames()) {
            jsonWriter.beginObject();
            jsonWriter.name("username").value(username.username());
            jsonWriter.name("date").value(username.date());
            jsonWriter.endObject();
        }
        jsonWriter.endArray();

        jsonWriter.name("notes");
        jsonWriter.beginArray();
        for (NoteEntry note : playerData.notes()) {
            jsonWriter.beginObject();
            jsonWriter.name("text").value(note.text());
            jsonWriter.name("date").value(note.date());
            jsonWriter.name("issuerName").value(note.issuerName());
            jsonWriter.endObject();
        }
        jsonWriter.endArray();

        jsonWriter.name("ipList");
        jsonWriter.beginArray();
        for (IpEntry ip : playerData.ipList()) {
            jsonWriter.beginObject();
            jsonWriter.name("ipAddress").value(ip.ipAddress());
            if (ip.country() != null) jsonWriter.name("country").value(ip.country());
            if (ip.region() != null) jsonWriter.name("region").value(ip.region());
            if (ip.asn() != null) jsonWriter.name("asn").value(ip.asn());
            if (ip.proxy() != null) jsonWriter.name("proxy").value(ip.proxy());
            if (ip.hosting() != null) jsonWriter.name("hosting").value(ip.hosting());
            jsonWriter.name("firstLogin").value(ip.firstLogin() != null ? ip.firstLogin() : "");
            jsonWriter.name("logins");
            jsonWriter.beginArray();
            if (ip.logins() != null) {
                for (String login : ip.logins()) {
                    if (login != null) jsonWriter.value(login);
                }
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
        }
        jsonWriter.endArray();

        jsonWriter.name("punishments");
        jsonWriter.beginArray();
        for (PunishmentEntry punishment : playerData.punishments()) {
            jsonWriter.beginObject();
            jsonWriter.name("_id").value(punishment.id());
            jsonWriter.name("type").value(punishment.type());
            jsonWriter.name("typeOrdinal").value(punishment.typeOrdinal());
            jsonWriter.name("reason").value(punishment.reason() != null ? punishment.reason() : defaultReason);
            jsonWriter.name("issued").value(punishment.issued() != null ? punishment.issued() : "");
            jsonWriter.name("issuerName").value(punishment.issuerName() != null ? punishment.issuerName() : Constants.DEFAULT_CONSOLE_NAME);
            jsonWriter.name("duration").value(punishment.duration());

            if (punishment.started() != null) jsonWriter.name("started").value(punishment.started());

            jsonWriter.name("notes");
            jsonWriter.beginArray();
            for (Map<String, Object> note : punishment.notes()) {
                jsonWriter.beginObject();
                String text = (String) note.get("text");
                String issuerName = (String) note.get("issuerName");
                String date = (String) note.get("date");
                jsonWriter.name("text").value(text != null ? text : "");
                jsonWriter.name("issuerName").value(issuerName != null ? issuerName : Constants.UNKNOWN);
                jsonWriter.name("date").value(date != null ? date : "");
                jsonWriter.endObject();
            }
            jsonWriter.endArray();

            jsonWriter.name("evidence");
            jsonWriter.beginArray();
            for (Object evidenceItem : punishment.evidence()) {
                if (evidenceItem instanceof String) {
                    jsonWriter.value((String) evidenceItem);
                } else {
                    gson.toJson(evidenceItem, Object.class, jsonWriter);
                }
            }
            jsonWriter.endArray();

            jsonWriter.name("attachedTicketIds");
            jsonWriter.beginArray();
            for (String ticketId : punishment.attachedTicketIds()) jsonWriter.value(ticketId);
            jsonWriter.endArray();

            jsonWriter.name("modifications");
            jsonWriter.beginArray();
            for (Object modification : punishment.modifications()) gson.toJson(modification, Object.class, jsonWriter);
            jsonWriter.endArray();

            if (punishment.data() != null && !punishment.data().isEmpty()) {
                jsonWriter.name("data");
                gson.toJson(punishment.data(), Map.class, jsonWriter);
            }

            jsonWriter.endObject();
        }
        jsonWriter.endArray();

        if (playerData.data() != null && !playerData.data().isEmpty()) {
            jsonWriter.name("data");
            gson.toJson(playerData.data(), Map.class, jsonWriter);
        }

        jsonWriter.endObject();
        jsonWriter.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        try {
            jsonWriter.endArray();
            jsonWriter.endObject();
        } finally {
            jsonWriter.close();
        }
    }

    public record PlayerData(String minecraftUuid, List<UsernameEntry> usernames, List<NoteEntry> notes,
                             List<IpEntry> ipList, List<PunishmentEntry> punishments, Map<String, Object> data) {
    }

    public record UsernameEntry(String username, String date) {
    }

    public record NoteEntry(String text, String date, String issuerName) {
    }

    public record IpEntry(String ipAddress, String country, String region, String asn, Boolean proxy, Boolean hosting,
                          String firstLogin, List<String> logins) {
    }

    public record PunishmentEntry(String id, String type, int typeOrdinal, String reason, String issued,
                                  String issuerName, Long duration, String started, Map<String, Object> data,
                                  List<Map<String, Object>> notes, List<Object> evidence,
                                  List<String> attachedTicketIds, List<Object> modifications) {
            public PunishmentEntry(String id, String type, int typeOrdinal, String reason, String issued,
                                   String issuerName, Long duration, String started, Map<String, Object> data,
                                   List<Map<String, Object>> notes, List<Object> evidence, List<String> attachedTicketIds,
                                   List<Object> modifications) {
                this.id = id;
                this.type = type;
                this.typeOrdinal = typeOrdinal;
                this.reason = reason;
                this.issued = issued;
                this.issuerName = issuerName;
                this.duration = duration;
                this.started = started;
                this.data = data;
                this.notes = notes != null ? notes : new ArrayList<>();
                this.evidence = evidence != null ? evidence : new ArrayList<>();
                this.attachedTicketIds = attachedTicketIds != null ? attachedTicketIds : new ArrayList<>();
                this.modifications = modifications != null ? modifications : new ArrayList<>();
            }
        }
}

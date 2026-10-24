package gg.modl.minecraft.core.impl.http.proto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import gg.modl.minecraft.api.Evidence;
import gg.modl.minecraft.api.Modification;
import gg.modl.minecraft.api.Note;
import gg.modl.minecraft.api.http.request.AddPunishmentEvidenceRequest;
import gg.modl.minecraft.api.http.request.AddPunishmentNoteRequest;
import gg.modl.minecraft.api.http.request.ChangePunishmentDurationRequest;
import gg.modl.minecraft.api.http.request.CreatePunishmentRequest;
import gg.modl.minecraft.api.http.request.ModifyPunishmentTicketsRequest;
import gg.modl.minecraft.api.http.request.PardonPunishmentRequest;
import gg.modl.minecraft.api.http.request.PunishmentCreateRequest;
import gg.modl.minecraft.api.http.request.StatWipeAcknowledgeRequest;
import gg.modl.minecraft.api.http.request.TogglePunishmentOptionRequest;
import gg.modl.minecraft.api.http.response.EvidenceUploadTokenResponse;
import gg.modl.minecraft.api.http.response.PunishmentCreateResponse;
import gg.modl.minecraft.api.http.response.PunishmentDetailResponse;
import gg.modl.minecraft.api.http.response.PunishmentPreviewResponse;
import gg.modl.minecraft.api.http.response.RecentPunishmentsResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PunishmentProtoMapper {

    private static final Gson GSON = new Gson();
    private static final Type STRING_OBJECT_MAP = new TypeToken<Map<String, Object>>() {
    }.getType();

    private PunishmentProtoMapper() {
    }

    public static gg.modl.proto.modl.v1.CreatePunishmentRequest toProto(CreatePunishmentRequest request) {
        gg.modl.proto.modl.v1.CreatePunishmentRequest.Builder builder =
            gg.modl.proto.modl.v1.CreatePunishmentRequest.newBuilder()
                .setTargetUuid(request.targetUuid())
                .setTypeOrdinal(request.typeOrdinal())
                .setDuration(request.duration());

        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        if (request.reason() != null) builder.setReason(request.reason());
        if (request.notes() != null) builder.addAllNotes(request.notes());
        if (request.attachedTicketIds() != null) builder.addAllAttachedTicketIds(request.attachedTicketIds());
        if (request.data() != null) builder.setData(ProtoConversions.mapToStruct(jsonObjectToMap(request.data())));
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.CreatePunishmentRequest toProto(PunishmentCreateRequest request) {
        gg.modl.proto.modl.v1.CreatePunishmentRequest.Builder builder =
            gg.modl.proto.modl.v1.CreatePunishmentRequest.newBuilder()
                .setTargetUuid(request.targetUuid())
                .setTypeOrdinal(request.typeOrdinal());

        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        if (request.reason() != null) builder.setReason(request.reason());
        if (request.severity() != null) builder.setSeverity(request.severity());
        if (request.status() != null) builder.setStatus(request.status());
        if (request.duration() != null) builder.setDuration(request.duration());
        if (request.notes() != null) builder.addAllNotes(request.notes());
        if (request.attachedTicketIds() != null) builder.addAllAttachedTicketIds(request.attachedTicketIds());
        if (request.data() != null) builder.setData(ProtoConversions.mapToStruct(request.data()));
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.AddPunishmentNoteRequest toProto(AddPunishmentNoteRequest request) {
        gg.modl.proto.modl.v1.AddPunishmentNoteRequest.Builder builder =
            gg.modl.proto.modl.v1.AddPunishmentNoteRequest.newBuilder()
                .setNote(request.note());
        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.AddPunishmentEvidenceRequest toProto(AddPunishmentEvidenceRequest request) {
        gg.modl.proto.modl.v1.AddPunishmentEvidenceRequest.Builder builder =
            gg.modl.proto.modl.v1.AddPunishmentEvidenceRequest.newBuilder()
                .setEvidenceUrl(request.evidenceUrl());
        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.ChangePunishmentDurationRequest toProto(ChangePunishmentDurationRequest request) {
        gg.modl.proto.modl.v1.ChangePunishmentDurationRequest.Builder builder =
            gg.modl.proto.modl.v1.ChangePunishmentDurationRequest.newBuilder();
        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        if (request.newDuration() != null) builder.setNewDuration(request.newDuration());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.TogglePunishmentOptionRequest toProto(TogglePunishmentOptionRequest request) {
        gg.modl.proto.modl.v1.TogglePunishmentOptionRequest.Builder builder =
            gg.modl.proto.modl.v1.TogglePunishmentOptionRequest.newBuilder()
                .setOption(request.option())
                .setEnabled(request.enabled());
        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.ModifyPunishmentTicketsRequest toProto(ModifyPunishmentTicketsRequest request) {
        gg.modl.proto.modl.v1.ModifyPunishmentTicketsRequest.Builder builder =
            gg.modl.proto.modl.v1.ModifyPunishmentTicketsRequest.newBuilder()
                .setModifyAssociatedTickets(request.modifyAssociatedTickets());
        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        if (request.addTicketIds() != null) builder.addAllAddTicketIds(request.addTicketIds());
        if (request.removeTicketIds() != null) builder.addAllRemoveTicketIds(request.removeTicketIds());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.PardonPunishmentRequest toProto(PardonPunishmentRequest request) {
        gg.modl.proto.modl.v1.PardonPunishmentRequest.Builder builder =
            gg.modl.proto.modl.v1.PardonPunishmentRequest.newBuilder();
        if (request.issuerName() != null) builder.setIssuerName(request.issuerName());
        if (request.issuerId() != null) builder.setIssuerId(request.issuerId());
        if (request.reason() != null) builder.setReason(request.reason());
        if (request.expectedType() != null) builder.setExpectedType(request.expectedType());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.StatWipeAcknowledgeRequest toProto(StatWipeAcknowledgeRequest request) {
        gg.modl.proto.modl.v1.StatWipeAcknowledgeRequest.Builder builder =
            gg.modl.proto.modl.v1.StatWipeAcknowledgeRequest.newBuilder()
                .setPunishmentId(request.punishmentId())
                .setSuccess(request.success());
        if (request.serverName() != null) builder.setServerName(request.serverName());
        return builder.build();
    }

    public static gg.modl.proto.modl.v1.CreateEvidenceUploadTokenRequest toUploadTokenRequest(String issuerName) {
        gg.modl.proto.modl.v1.CreateEvidenceUploadTokenRequest.Builder builder =
            gg.modl.proto.modl.v1.CreateEvidenceUploadTokenRequest.newBuilder();
        if (issuerName != null) builder.setIssuerName(issuerName);
        return builder.build();
    }

    public static PunishmentCreateResponse toPunishmentCreateResponse(gg.modl.proto.modl.v1.PunishmentCreateResponse proto) {
        return new PunishmentCreateResponse(proto.getMessage(), proto.getPunishmentId(), proto.getStatus());
    }

    public static EvidenceUploadTokenResponse toUploadTokenResponse(gg.modl.proto.modl.v1.EvidenceUploadTokenResponse proto) {
        return new EvidenceUploadTokenResponse(ProtoConversions.emptyToNull(proto.getToken()), proto.getStatus());
    }

    public static PunishmentPreviewResponse toPreviewResponse(gg.modl.proto.modl.v1.PunishmentPreviewResponse proto) {
        return PunishmentPreviewResponse.builder()
            .message(proto.getMessage())
            .socialStatus(proto.getSocialStatus())
            .gameplayStatus(proto.getGameplayStatus())
            .offenderStatus(proto.getOffenderStatus())
            .category(proto.getCategory())
            .success(proto.getSuccess())
            .singleSeverityPunishment(proto.getSingleSeverityPunishment())
            .permanentUntilUsernameChange(proto.getPermanentUntilUsernameChange())
            .permanentUntilSkinChange(proto.getPermanentUntilSkinChange())
            .canBeAltBlocking(proto.getCanBeAltBlocking())
            .canBeStatWiping(proto.getCanBeStatWiping())
            .status(proto.getStatus())
            .socialPoints(proto.getSocialPoints())
            .gameplayPoints(proto.getGameplayPoints())
            .lenient(proto.hasLenient() ? toSeverityPreview(proto.getLenient()) : null)
            .regular(proto.hasRegular() ? toSeverityPreview(proto.getRegular()) : null)
            .aggravated(proto.hasAggravated() ? toSeverityPreview(proto.getAggravated()) : null)
            .singleSeverity(proto.hasSingleSeverity() ? toSeverityPreview(proto.getSingleSeverity()) : null)
            .build();
    }

    private static PunishmentPreviewResponse.SeverityPreview toSeverityPreview(
        gg.modl.proto.modl.v1.PunishmentPreviewResponse.SeverityPreview proto) {
        return PunishmentPreviewResponse.SeverityPreview.builder()
            .severity(proto.getSeverity())
            .durationFormatted(proto.getDurationFormatted())
            .punishmentType(proto.getPunishmentType())
            .newSocialStatus(proto.getNewSocialStatus())
            .newGameplayStatus(proto.getNewGameplayStatus())
            .permanent(proto.getPermanent())
            .points(proto.getPoints())
            .newSocialPoints(proto.getNewSocialPoints())
            .newGameplayPoints(proto.getNewGameplayPoints())
            .durationMs(proto.getDurationMs())
            .build();
    }

    public static PunishmentDetailResponse toDetailResponse(gg.modl.proto.modl.v1.PunishmentDetailResponse proto) {
        gg.modl.proto.modl.v1.PunishmentDetailResponse.PunishmentDetailEntry entry = proto.getPunishment();

        List<Object> modifications = new ArrayList<>();
        entry.getModificationsList().forEach(s -> modifications.add(ProtoConversions.structToMap(s)));

        List<Object> notes = new ArrayList<>();
        entry.getNotesList().forEach(s -> notes.add(ProtoConversions.structToMap(s)));

        List<Object> evidence = new ArrayList<>();
        entry.getEvidenceList().forEach(s -> evidence.add(ProtoConversions.structToMap(s)));

        PunishmentDetailResponse.PunishmentDetail detail = PunishmentDetailResponse.PunishmentDetail.builder()
            .id(entry.getId())
            .playerUuid(entry.getPlayerUuid())
            .playerName(entry.getPlayerName())
            .issuerName(entry.getIssuerName())
            .issued(entry.getIssued())
            .started(entry.getStarted())
            .type(entry.getType())
            .typeOrdinal(entry.getTypeOrdinal())
            .data(ProtoConversions.structToMap(entry.getData()))
            .modifications(modifications)
            .notes(notes)
            .evidence(evidence)
            .build();

        return new PunishmentDetailResponse(detail, proto.getStatus());
    }

    public static RecentPunishmentsResponse toRecentResponse(gg.modl.proto.modl.v1.RecentPunishmentsResponse proto) {
        List<RecentPunishmentsResponse.RecentPunishment> punishments = new ArrayList<>();
        proto.getPunishmentsList().forEach(p -> punishments.add(toRecentPunishment(p)));
        return new RecentPunishmentsResponse(punishments, proto.getStatus());
    }

    private static RecentPunishmentsResponse.RecentPunishment toRecentPunishment(
        gg.modl.proto.modl.v1.RecentPunishmentsResponse.RecentPunishment proto) {
        List<Modification> modifications = new ArrayList<>();
        proto.getModificationsList().forEach(m -> modifications.add(PlayerProtoMapper.toModification(m)));

        List<Note> notes = new ArrayList<>();
        proto.getNotesList().forEach(n -> notes.add(new Note(
            n.getText(),
            ProtoConversions.dateFromMillis(n.getDate()),
            n.hasIssuerName() ? n.getIssuerName() : null,
            n.hasIssuerId() ? n.getIssuerId() : null)));

        List<Evidence> evidence = new ArrayList<>();
        proto.getEvidenceList().forEach(e -> evidence.add(PlayerProtoMapper.toEvidence(e)));

        return RecentPunishmentsResponse.RecentPunishment.builder()
            .playerName(proto.getPlayerName())
            .playerUuid(proto.getPlayerUuid())
            .id(proto.getId())
            .issuerName(proto.getIssuerName())
            .issued(ProtoConversions.dateFromMillis(proto.getIssued()))
            .started(proto.hasStarted() ? ProtoConversions.dateFromMillis(proto.getStarted()) : null)
            .type(proto.getType())
            .typeOrdinal(proto.hasTypeOrdinal() ? proto.getTypeOrdinal() : null)
            .modifications(modifications)
            .notes(notes)
            .evidence(evidence)
            .attachedTicketIds(new ArrayList<>(proto.getAttachedTicketIdsList()))
            .data(proto.hasData() ? ProtoConversions.structToMap(proto.getData()) : null)
            .build();
    }

    private static Map<String, Object> jsonObjectToMap(JsonObject json) {
        return GSON.fromJson(json, STRING_OBJECT_MAP);
    }
}

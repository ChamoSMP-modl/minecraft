package gg.modl.minecraft.core.impl.commands.staff.punishments;

import dev.simplix.cirrus.player.CirrusPlayerWrapper;
import gg.modl.minecraft.api.Account;
import gg.modl.minecraft.api.PunishmentTypeClassifier;
import gg.modl.minecraft.api.RebuildablePunishmentTypeClassifier;
import gg.modl.minecraft.api.http.request.PunishmentCreateRequest;
import gg.modl.minecraft.api.http.response.PunishmentCreateResponse;
import gg.modl.minecraft.api.http.response.PunishmentTypesResponse;
import gg.modl.minecraft.core.HttpClientHolder;
import gg.modl.minecraft.core.Platform;
import gg.modl.minecraft.core.cache.Cache;
import gg.modl.minecraft.core.command.StaffOrPermission;
import gg.modl.minecraft.core.impl.menus.inspect.PunishMenu;
import gg.modl.minecraft.core.impl.menus.util.MenuAsync;
import gg.modl.minecraft.core.integration.mojang.MojangProfiles;
import gg.modl.minecraft.core.integration.mojang.WebPlayer;
import gg.modl.minecraft.core.locale.LocaleManager;
import gg.modl.minecraft.core.punishment.PunishmentFlagParser;
import gg.modl.minecraft.core.punishment.PunishmentIssuer;
import gg.modl.minecraft.core.punishment.PunishmentTypeParser;
import gg.modl.minecraft.core.staff.PermissionUtil;
import gg.modl.minecraft.core.staff.StaffPermissionService;
import gg.modl.minecraft.core.util.CommandUtil;
import gg.modl.minecraft.core.util.Constants;
import gg.modl.minecraft.core.util.Permissions;
import lombok.RequiredArgsConstructor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.command.CommandActor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static gg.modl.minecraft.core.util.Java8Collections.mapOf;
import static gg.modl.minecraft.core.util.Java8Collections.setOf;

@RequiredArgsConstructor
@Command("punish")
public class PunishCommand {
    private static final Set<String> VALID_SEVERITIES = setOf("low", "regular", "severe");
    private static final String DEFAULT_SEVERITY = "regular";
    private static final int MANUAL_PUNISHMENT_MAX_ORDINAL = PunishmentTypeClassifier.ORDINAL_BLACKLIST, MAX_TYPE_WORD_LENGTH = 4;

    private final HttpClientHolder httpClientHolder;
    private final Platform platform;
    private final Cache cache;
    private final LocaleManager localeManager;
    private final RebuildablePunishmentTypeClassifier punishmentTypeClassifier;
    private final StaffPermissionService staffPermissionService;

    private volatile List<PunishmentTypesResponse.PunishmentTypeData> cachedPunishmentTypes = new ArrayList<>();
    private volatile boolean cacheInitialized = false;

    @Description("Issue a punishment to a player. With no type specified and as a player, opens the punishment GUI.")
    @StaffOrPermission(value = Permissions.PUNISH, needsPermissionOnStaff = false)
    public void punish(CommandActor actor, @Named("target") Account target, @Named("args") String[] args) {
        if (target == null) {
            actor.reply(localeManager.getPunishmentMessage("general.player_not_found", mapOf()));
            return;
        }

        if ((args == null || args.length == 0) && actor.uniqueId() != null) {
            if (platform.areMenusAvailable()) {
                openPunishmentGui(actor, target);
            } else actor.reply(localeManager.getMessage("general.gui_unavailable"));
            return;
        }

        if (args == null || args.length == 0) {
            actor.reply(localeManager.getPunishmentMessage("general.invalid_syntax", mapOf()));
            return;
        }

        if (!cacheInitialized || cachedPunishmentTypes.isEmpty()) {
            actor.reply(localeManager.getPunishmentMessage("general.punishment_types_not_loaded", mapOf()));
            return;
        }

        List<PunishmentTypesResponse.PunishmentTypeData> punishmentTypes = cachedPunishmentTypes;
        ParsedCommand parsed = parsePunishmentTypeAndArgs(args, punishmentTypes);
        if (parsed == null) {
            String availableTypes = punishmentTypes.stream()
                    .map(PunishmentTypesResponse.PunishmentTypeData::name)
                    .collect(Collectors.joining(", "));
            actor.reply(localeManager.getPunishmentMessage("general.invalid_punishment_type",
                    mapOf("types", availableTypes)));
            return;
        }

        final PunishmentTypesResponse.PunishmentTypeData punishmentType = parsed.punishmentType;
        String punishmentPermission = PermissionUtil.formatPunishmentPermission(punishmentType.name());
        if (!PermissionUtil.hasPermission(actor, cache, punishmentPermission)) {
            actor.reply(localeManager.getPunishmentMessage("general.no_permission_punishment",
                    mapOf("type", punishmentType.name())));
            return;
        }

        PunishmentFlagParser.Flags punishmentArgs = PunishmentFlagParser.builder()
                .severity(true).silent(true).altBlocking(true).statWipe(true).build()
                .parse(parsed.remainingArgs);
        if (punishmentArgs.getSeverity() != null && !VALID_SEVERITIES.contains(punishmentArgs.getSeverity())) {
            actor.reply(localeManager.getMessage("punishment_commands.invalid_severity"));
            return;
        }

        String validationError = validatePunishmentCompatibility(punishmentArgs, punishmentType);
        if (validationError != null) {
            actor.reply(validationError);
            return;
        }

        String severity = punishmentArgs.getSeverity() == null ? DEFAULT_SEVERITY : punishmentArgs.getSeverity();
        final String issuerName = CommandUtil.resolveActorName(actor, cache, platform);
        final String issuerId = CommandUtil.resolveActorId(actor, cache);
        Map<String, Object> data = buildPunishmentData(punishmentArgs, punishmentType, target);

        data.put("issuedServer", actor.uniqueId() != null
                ? platform.getPlayerServer(actor.uniqueId())
                : platform.getServerName());

        List<String> notes = new ArrayList<>();
        if (!punishmentArgs.getReason().isEmpty()) notes.add(punishmentArgs.getReason());

        PunishmentCreateRequest request = new PunishmentCreateRequest(
                target.getMinecraftUuid().toString(),
                issuerName,
                issuerId,
                punishmentArgs.getReason().isEmpty() ? localeManager.getMessage("config.default_reason") : punishmentArgs.getReason(),
                severity,
                null,
                punishmentType.ordinal(),
                punishmentArgs.getDuration() > 0 ? punishmentArgs.getDuration() : null,
                data,
                notes,
                new ArrayList<>()
        );

        final String punishmentTypeName = punishmentType.name();
        final String targetName = target.getUsernames().get(0).getUsername();

        CompletableFuture<PunishmentCreateResponse> future = httpClientHolder.client().createPunishmentWithResponse(request);
        new PunishmentIssuer(platform, localeManager).issue(actor, future, punishmentTypeName, targetName, null);
    }

    private void openPunishmentGui(CommandActor actor, Account target) {
        UUID senderUuid = actor.uniqueId();
        String senderName = CommandUtil.resolveSenderName(senderUuid, cache, platform);

        PunishMenu menu = new PunishMenu(
                platform, httpClientHolder.client(), senderUuid, senderName, target, null
        );
        CirrusPlayerWrapper player = platform.getPlayerWrapper(senderUuid);
        MenuAsync.displayWhenLoaded(platform, menu.getDataFuture(), player, menu::display);
    }

    public void initializePunishmentTypes() {
        httpClientHolder.client().getPunishmentTypes().thenAccept(response -> {
            if (response.isSuccess()) {
                PunishmentTypeParser.populate(punishmentTypeClassifier, response.getData());
                cachedPunishmentTypes = response.getData().stream()
                        .filter(pt -> pt.ordinal() > MANUAL_PUNISHMENT_MAX_ORDINAL)
                        .collect(Collectors.toList());
                cacheInitialized = true;
                platform.runOnMainThread(() ->
                        platform.log("Loaded " + cachedPunishmentTypes.size() + " punishment types from API"));
            } else {
                platform.runOnMainThread(() ->
                        platform.log("Failed to load punishment types from API: " + response.getStatus()));
            }
        }).exceptionally(throwable -> {
            platform.runOnMainThread(() ->
                    platform.log("Error loading punishment types: " + throwable.getMessage()));
            return null;
        });

        staffPermissionService.reload();
    }

    public List<String> getPunishmentTypeNames() {
        return cachedPunishmentTypes.stream()
                .map(PunishmentTypesResponse.PunishmentTypeData::name)
                .collect(Collectors.toList());
    }

    public void updatePunishmentTypesCache(List<PunishmentTypesResponse.PunishmentTypeData> allTypes) {
        PunishmentTypeParser.populate(punishmentTypeClassifier, allTypes);
        cachedPunishmentTypes = allTypes.stream()
                .filter(pt -> pt.ordinal() > MANUAL_PUNISHMENT_MAX_ORDINAL)
                .collect(Collectors.toList());
        cacheInitialized = true;
    }

    private Map<String, Object> buildPunishmentData(PunishmentFlagParser.Flags args, PunishmentTypesResponse.PunishmentTypeData punishmentType, Account target) {
        Map<String, Object> data = new HashMap<>();
        data.put("duration", 0L);
        data.put("blockedName", resolveBlockedName(punishmentType, target));
        data.put("blockedSkin", resolveBlockedSkin(punishmentType, target));
        data.put("linkedBanId", null);
        data.put("linkedBanExpiry", null);
        data.put("chatLog", null);
        data.put("altBlocking", Boolean.TRUE.equals(punishmentType.canBeAltBlocking()) && args.isAltBlocking());
        data.put("wipeAfterExpiry", Boolean.TRUE.equals(punishmentType.canBeStatWiping()) && args.isStatWipe());
        data.put("silent", args.isSilent());

        if (args.getDuration() > 0) data.put("duration", args.getDuration());

        return data;
    }

    private String resolveBlockedName(PunishmentTypesResponse.PunishmentTypeData punishmentType, Account target) {
        if (!Boolean.TRUE.equals(punishmentType.permanentUntilUsernameChange())) return null;
        return !target.getUsernames().isEmpty()
                ? target.getUsernames().get(target.getUsernames().size() - 1).getUsername()
                : Constants.UNKNOWN;
    }

    private String resolveBlockedSkin(PunishmentTypesResponse.PunishmentTypeData punishmentType, Account target) {
        if (!Boolean.TRUE.equals(punishmentType.permanentUntilSkinChange())) return null;
        try {
            WebPlayer webPlayer = MojangProfiles.client().getSync(target.getMinecraftUuid());
            return (webPlayer != null && webPlayer.valid()) ? webPlayer.skin() : null;
        } catch (Exception e) {
            Logger.getLogger("modl").warning(
                    "Failed to get skin hash for " + target.getUsernames().get(0).getUsername() + ": " + e.getMessage());
            return null;
        }
    }

    private String validatePunishmentCompatibility(PunishmentFlagParser.Flags args, PunishmentTypesResponse.PunishmentTypeData punishmentType) {
        if (Boolean.TRUE.equals(punishmentType.singleSeverityPunishment()) && args.getSeverity() != null)
            return localeManager.getPunishmentMessage("validation.single_severity_error",
                    mapOf("type", punishmentType.name()));
        if (Boolean.TRUE.equals(punishmentType.permanentUntilSkinChange()) && args.getSeverity() != null)
            return localeManager.getPunishmentMessage("validation.permanent_skin_change_error",
                    mapOf("type", punishmentType.name()));
        if (Boolean.TRUE.equals(punishmentType.permanentUntilUsernameChange()) && args.getSeverity() != null)
            return localeManager.getPunishmentMessage("validation.permanent_username_change_error",
                    mapOf("type", punishmentType.name()));
        if (args.isAltBlocking() && !Boolean.TRUE.equals(punishmentType.canBeAltBlocking()))
            return localeManager.getPunishmentMessage("validation.alt_blocking_not_supported",
                    mapOf("type", punishmentType.name()));
        if (args.isStatWipe() && !Boolean.TRUE.equals(punishmentType.canBeStatWiping()))
            return localeManager.getPunishmentMessage("validation.stat_wiping_not_supported",
                    mapOf("type", punishmentType.name()));
        return null;
    }

    private ParsedCommand parsePunishmentTypeAndArgs(String[] args, List<PunishmentTypesResponse.PunishmentTypeData> punishmentTypes) {
        for (int i = Math.min(args.length, MAX_TYPE_WORD_LENGTH); i >= 1; i--) {
            String potentialType = String.join(" ", Arrays.copyOfRange(args, 0, i));

            Optional<PunishmentTypesResponse.PunishmentTypeData> matchedType = punishmentTypes.stream()
                    .filter(pt -> pt.name().equalsIgnoreCase(potentialType))
                    .findFirst();

            if (matchedType.isPresent()) {
                String remainingArgs = (i < args.length) ? String.join(" ", Arrays.copyOfRange(args, i, args.length)) : "";
                return new ParsedCommand(matchedType.get(), remainingArgs);
            }
        }
        return null;
    }

    private record ParsedCommand(PunishmentTypesResponse.PunishmentTypeData punishmentType, String remainingArgs) {
    }
}

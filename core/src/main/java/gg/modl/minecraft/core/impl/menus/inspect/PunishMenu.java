package gg.modl.minecraft.core.impl.menus.inspect;

import gg.modl.minecraft.core.PluginServices;
import dev.simplix.cirrus.item.CirrusItem;
import dev.simplix.cirrus.item.CirrusItemType;
import dev.simplix.cirrus.model.CallResult;
import dev.simplix.cirrus.model.Click;
import dev.simplix.cirrus.player.CirrusPlayerWrapper;
import dev.simplix.cirrus.text.CirrusChatElement;
import gg.modl.minecraft.api.Account;
import gg.modl.minecraft.api.http.ModlHttpClient;
import gg.modl.minecraft.api.http.response.PunishmentPreviewResponse;
import gg.modl.minecraft.api.http.response.PunishmentTypesResponse;
import gg.modl.minecraft.core.Platform;
import gg.modl.minecraft.core.config.PunishGuiConfig;
import gg.modl.minecraft.core.cache.Cache;
import gg.modl.minecraft.core.impl.menus.base.BaseInspectMenu;
import gg.modl.minecraft.core.impl.menus.util.InspectTabItems.InspectTab;
import gg.modl.minecraft.core.impl.menus.util.MenuAsync;
import gg.modl.minecraft.core.impl.menus.util.MenuItems;
import gg.modl.minecraft.core.staff.PermissionUtil;
import lombok.Getter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PunishMenu extends BaseInspectMenu {
    private List<PunishmentTypesResponse.PunishmentTypeData> punishmentTypes = new ArrayList<>();
    private final Map<Integer, PunishmentTypesResponse.PunishmentTypeData> typesByOrdinal = new HashMap<>();
    private final Map<Integer, PunishmentPreviewResponse> previewByOrdinal = new ConcurrentHashMap<>();
    private final PunishGuiConfig guiConfig;
    @Getter private final CompletableFuture<Void> dataFuture;

    private static final int[] GUI_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final int PREVIEW_WINDOW_SIZE = 4;

    public PunishMenu(Platform platform, ModlHttpClient httpClient, UUID viewerUuid, String viewerName,
                      Account targetAccount, Consumer<CirrusPlayerWrapper> backAction) {
        super(platform, httpClient, viewerUuid, viewerName, targetAccount, backAction);

        title("Punish: " + targetName);
        activeTab = InspectTab.PUNISH;

        Cache configCache = PluginServices.cache();
        PunishGuiConfig cached = configCache != null ? configCache.getCachedPunishGuiConfig() : null;
        if (cached != null) {
            this.guiConfig = cached;
        } else {
            this.guiConfig = PunishGuiConfig.load(platform.getDataFolder().toPath(),
                    platform.getLogger());
            if (configCache != null) {
                configCache.cachePunishGuiConfig(this.guiConfig);
            }
        }

        buildHeader();
        this.dataFuture = loadPunishmentTypes()
                .thenCompose(v -> loadPreviews())
                .thenRun(this::buildPunishmentGrid);
    }

    private CompletableFuture<Void> loadPunishmentTypes() {
        Cache cache = PluginServices.cache();

        PunishmentTypesResponse cached = cache != null ? cache.getCachedPunishmentTypes() : null;
        if (cached != null && cached.isSuccess() && cached.getData() != null) {
            applyPunishmentTypes(cached.getData());
            return CompletableFuture.completedFuture(null);
        }

        return httpClient.getPunishmentTypes().thenAccept(response -> {
            if (response.isSuccess() && response.getData() != null) {
                applyPunishmentTypes(response.getData());
                if (cache != null) cache.cachePunishmentTypes(response);
            }
        }).exceptionally(e -> null);
    }

    private void applyPunishmentTypes(List<PunishmentTypesResponse.PunishmentTypeData> data) {
        punishmentTypes = new ArrayList<>(data);
        for (PunishmentTypesResponse.PunishmentTypeData type : punishmentTypes) {
            typesByOrdinal.put(type.ordinal(), type);
        }
    }

    private CompletableFuture<Void> loadPreviews() {
        List<Integer> pendingOrdinals = new ArrayList<>();
        for (PunishGuiConfig.PunishSlotConfig slotConfig : guiConfig.getEnabledSlots()) {
            int ordinal = slotConfig.getOrdinal();
            if (!typesByOrdinal.containsKey(ordinal) || previewByOrdinal.containsKey(ordinal)) continue;
            pendingOrdinals.add(ordinal);
        }
        return loadPreviewWindow(pendingOrdinals, 0);
    }

    private CompletableFuture<Void> loadPreviewWindow(List<Integer> ordinals, int windowStart) {
        if (windowStart >= ordinals.size()) return CompletableFuture.completedFuture(null);

        int windowEnd = Math.min(windowStart + PREVIEW_WINDOW_SIZE, ordinals.size());
        List<CompletableFuture<Void>> window = new ArrayList<>();
        for (int i = windowStart; i < windowEnd; i++) {
            int ordinal = ordinals.get(i);
            window.add(httpClient.getPunishmentPreview(targetUuid, ordinal).thenAccept(response -> {
                if (response.isSuccess()) previewByOrdinal.put(ordinal, response);
            }).exceptionally(e -> null));
        }
        return CompletableFuture.allOf(window.toArray(new CompletableFuture[0]))
                .thenCompose(v -> loadPreviewWindow(ordinals, windowEnd));
    }

    private void buildPunishmentGrid() {
        Cache cache = PluginServices.cache();

        for (int configSlot = 1; configSlot <= 14; configSlot++) {
            PunishGuiConfig.PunishSlotConfig slotConfig = guiConfig.getSlot(configSlot);

            if (slotConfig == null || !slotConfig.isEnabled()) continue;

            int guiSlot = GUI_SLOTS[configSlot - 1];

            PunishmentTypesResponse.PunishmentTypeData type = typesByOrdinal.get(slotConfig.getOrdinal());

            if (type != null) {
                String permission = PermissionUtil.formatPunishmentPermission(type.name());
                boolean hasPermission = cache != null && cache.hasPermission(viewerUuid, permission);

                if (hasPermission)
                    set(createPunishmentTypeItem(type, slotConfig).slot(guiSlot));
                else
                    set(createDisabledPunishmentTypeItem(slotConfig).slot(guiSlot));
            } else {
                set(createPlaceholderItem(slotConfig).slot(guiSlot));
            }
        }

        if (punishmentTypes.isEmpty() && guiConfig.getEnabledSlots().isEmpty()) {
            set(CirrusItem.of(
                    CirrusItemType.BARRIER,
                    CirrusChatElement.ofMiniMessage(MenuItems.COLOR_RED + "No Punishment Types"),
                    MenuItems.lore(
                            MenuItems.COLOR_GRAY + "Failed to load punishment types",
                            MenuItems.COLOR_GRAY + "from the API"
                    )
            ).slot(31));
        }
    }

    private CirrusItem createPunishmentTypeItem(PunishmentTypesResponse.PunishmentTypeData type,
                                                 PunishGuiConfig.PunishSlotConfig slotConfig) {
        List<String> lore = new ArrayList<>();

        for (String line : slotConfig.getDescription()) {
            String processed = processPlaceholder(line, type);
            lore.add(processed);
        }

        Boolean singleSeverity = type.singleSeverityPunishment();
        Boolean permUsername = type.permanentUntilUsernameChange();
        Boolean permSkin = type.permanentUntilSkinChange();

        boolean isSingleType = (singleSeverity != null && singleSeverity) ||
                               (permUsername != null && permUsername) ||
                               (permSkin != null && permSkin);

        if (lore.isEmpty() || !lore.get(lore.size() - 1).contains("Click")) {
            lore.add("");
            if (isSingleType) {
                lore.add(MenuItems.COLOR_YELLOW + "Click to issue punishment");
            } else {
                lore.add(MenuItems.COLOR_YELLOW + "Click to select severity");
            }
        }

        CirrusItemType itemType = parseItemType(slotConfig.getItem());

        return CirrusItem.of(
                itemType,
                CirrusChatElement.ofMiniMessage(MenuItems.COLOR_RED + slotConfig.getTitle()),
                MenuItems.lore(lore)
        ).actionHandler("punishType_" + type.ordinal());
    }

    private CirrusItem createDisabledPunishmentTypeItem(PunishGuiConfig.PunishSlotConfig slotConfig) {
        CirrusItemType itemType = parseItemType(slotConfig.getItem());

        return CirrusItem.of(
                itemType,
                CirrusChatElement.ofMiniMessage(MenuItems.COLOR_GRAY + slotConfig.getTitle()),
                MenuItems.lore(
                        MenuItems.COLOR_RED + "No Permission",
                        MenuItems.COLOR_GRAY + "You don't have permission",
                        MenuItems.COLOR_GRAY + "to issue this punishment type"
                )
        );
    }

    private CirrusItem createPlaceholderItem(PunishGuiConfig.PunishSlotConfig slotConfig) {
        CirrusItemType itemType = parseItemType(slotConfig.getItem());

        return CirrusItem.of(
                itemType,
                CirrusChatElement.ofMiniMessage(MenuItems.COLOR_GRAY + slotConfig.getTitle()),
                MenuItems.lore(
                        MenuItems.COLOR_RED + "Punishment type not found",
                        MenuItems.COLOR_GRAY + "Ordinal: " + slotConfig.getOrdinal()
                )
        );
    }

    private String processPlaceholder(String line, PunishmentTypesResponse.PunishmentTypeData type) {
        String result = line;

        result = result.replace("<gray>", MenuItems.COLOR_GRAY);
        result = result.replace("<white>", MenuItems.COLOR_WHITE);
        result = result.replace("<red>", MenuItems.COLOR_RED);
        result = result.replace("<green>", MenuItems.COLOR_GREEN);
        result = result.replace("<yellow>", MenuItems.COLOR_YELLOW);
        result = result.replace("<gold>", MenuItems.COLOR_GOLD);
        result = result.replace("<aqua>", MenuItems.COLOR_AQUA);

        if (type.staffDescription() != null)
            result = result.replace("{staff-description}", type.staffDescription());
        else
            result = result.replace("{staff-description}", "");

        PunishmentPreviewResponse preview = previewByOrdinal.get(type.ordinal());
        if (preview != null) {
            result = result.replace("{social-status}", preview.getSocialStatus() != null ?
                    preview.getSocialStatus() : "Unknown");
            result = result.replace("{gameplay-status}", preview.getGameplayStatus() != null ?
                    preview.getGameplayStatus() : "Unknown");
            result = result.replace("{social-points}", String.valueOf(preview.getSocialPoints()));
            result = result.replace("{gameplay-points}", String.valueOf(preview.getGameplayPoints()));
        } else {
            result = result.replace("{social-status}", "Loading...");
            result = result.replace("{gameplay-status}", "Loading...");
            result = result.replace("{social-points}", "?");
            result = result.replace("{gameplay-points}", "?");
        }

        return result;
    }

    private CirrusItemType parseItemType(String itemString) {
        if (itemString == null || itemString.isEmpty()) return CirrusItemType.PAPER;

        return CirrusItemType.of(itemString);
    }

    @Override
    protected void registerActionHandlers() {
        super.registerActionHandlers();

        for (PunishmentTypesResponse.PunishmentTypeData type : punishmentTypes) {
            registerActionHandler("punishType_" + type.ordinal(), click -> {
                handlePunishmentType(click, type);
                return CallResult.DENY_GRABBING;
            });
        }

        registerActionHandler("openPunish", click -> {});
    }

    private void handlePunishmentType(Click click, PunishmentTypesResponse.PunishmentTypeData type) {
        Consumer<CirrusPlayerWrapper> backToPunish = player -> {
            PunishMenu m = new PunishMenu(platform, httpClient, viewerUuid, viewerName, targetAccount, backAction);
            MenuAsync.displayWhenLoaded(platform, m.getDataFuture(), player, m::display);
        };

        PunishSeverityMenu severityMenu = new PunishSeverityMenu(platform, httpClient, viewerUuid, viewerName,
                targetAccount, type, backAction, backToPunish);
        MenuAsync.displayWhenLoaded(platform, severityMenu.getDataFuture(), click.player(), severityMenu::display);
    }
}

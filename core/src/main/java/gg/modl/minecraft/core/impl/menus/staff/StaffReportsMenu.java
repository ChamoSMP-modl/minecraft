package gg.modl.minecraft.core.impl.menus.staff;

import gg.modl.minecraft.core.PluginServices;
import dev.simplix.cirrus.item.CirrusItem;
import dev.simplix.cirrus.item.CirrusItemType;
import dev.simplix.cirrus.model.CirrusClickType;
import dev.simplix.cirrus.model.Click;
import dev.simplix.cirrus.player.CirrusPlayerWrapper;
import dev.simplix.cirrus.text.CirrusChatElement;
import gg.modl.minecraft.api.http.ModlHttpClient;
import gg.modl.minecraft.api.http.response.ReportsResponse;
import gg.modl.minecraft.core.Platform;
import gg.modl.minecraft.core.impl.menus.base.BaseStaffListMenu;
import gg.modl.minecraft.core.impl.menus.inspect.InspectMenu;
import gg.modl.minecraft.core.impl.menus.util.MenuItems;
import gg.modl.minecraft.core.impl.menus.util.MenuSlots;
import gg.modl.minecraft.core.impl.menus.util.ReportRenderUtil;
import gg.modl.minecraft.core.impl.menus.util.MenuAsync;
import gg.modl.minecraft.core.impl.menus.util.StaffTabItems.StaffTab;
import gg.modl.minecraft.core.locale.LocaleManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class StaffReportsMenu extends BaseStaffListMenu<StaffReportsMenu.Report> {
    public record Report(String id, String type, String reporterName, String reportedPlayerName, String content,
                         String status, UUID reportedPlayerUuid, Date date) {

    }

    private final List<Report> reports = new ArrayList<>();
    private String currentFilter = "all", currentStatusFilter = "open";
    private final List<String> filterOptions = Arrays.asList("all", "gameplay", "chat");
    @Getter private final CompletableFuture<Void> dataFuture;

    public StaffReportsMenu(Platform platform, ModlHttpClient httpClient, UUID viewerUuid, String viewerName,
                            boolean isAdmin, String panelUrl, Consumer<CirrusPlayerWrapper> backAction) {
        super("Reports", platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction);
        activeTab = StaffTab.REPORTS;

        this.dataFuture = fetchReports();
    }

    private CompletableFuture<Void> fetchReports() {
        return httpClient.getReports("all").thenAccept(response -> {
            if (response.isSuccess() && response.getReports() != null) {
                reports.clear();
                for (ReportsResponse.Report report : response.getReports()) {
                    reports.add(new Report(
                            report.getId(),
                            ReportRenderUtil.normalizeReportType(report),
                            report.getReporterName(),
                            report.getReportedPlayerName(),
                            ReportRenderUtil.getReportContent(report),
                            report.getStatus(),
                            ReportRenderUtil.parseReportedPlayerUuid(report),
                            report.getCreatedAt()
                    ));
                }
            }
        }).exceptionally(e -> null);
    }

    public StaffReportsMenu withFilter(String filter) {
        this.currentFilter = filter;
        return this;
    }

    public StaffReportsMenu withStatusFilter(String statusFilter) {
        this.currentStatusFilter = statusFilter;
        return this;
    }

    @Override
    protected Map<Integer, CirrusItem> intercept(int menuSize) {
        Map<Integer, CirrusItem> items = super.intercept(menuSize);

        items.put(MenuSlots.FILTER_BUTTON, MenuItems.filterButton(currentFilter, filterOptions, currentStatusFilter, "reports"));

        return items;
    }

    @Override
    protected Collection<Report> elements() {
        if (reports.isEmpty())
            return Collections.singletonList(new Report(null, null, null, null, null, null, null, null));

        List<Report> filtered = new ArrayList<>();

        for (Report report : reports) {
            boolean typeMatch = currentFilter.equals("all") || (report.type() != null && report.type().equalsIgnoreCase(currentFilter));
            boolean statusMatch = ReportRenderUtil.matchesStatusFilter(currentStatusFilter, report.status());
            if (typeMatch && statusMatch)
                filtered.add(report);
        }

        if (filtered.isEmpty())
            return Collections.singletonList(new Report(null, null, null, null, null, null, null, null));

        filtered.sort((r1, r2) -> {
            if (r1.date() == null && r2.date() == null) return 0;
            if (r1.date() == null) return 1;
            if (r2.date() == null) return -1;
            return r2.date().compareTo(r1.date());
        });
        return filtered;
    }

    @Override
    protected CirrusItem map(Report report) {
        LocaleManager locale = PluginServices.locale();

        if (report.id() == null) return createEmptyPlaceholder(locale.getMessage("menus.empty.staff_reports"));

        Map<String, String> vars = new HashMap<>();
        vars.put("id", report.id());
        vars.put("type", report.type() != null ? report.type() : "Unknown");
        vars.put("reporter", report.reporterName() != null ? report.reporterName() : "Unknown");
        vars.put("reported", report.reportedPlayerName() != null ? report.reportedPlayerName() : "Unknown");
        vars.put("date", MenuItems.formatDate(report.date()));
        vars.put("status", report.status() != null && report.status().equalsIgnoreCase("closed")
                ? "<red>CLOSED" : "<green>OPEN");
        vars.put("content", String.join("\n", ReportRenderUtil.processContent(report.content())));

        List<String> lore = ReportRenderUtil.buildLore(locale, "menus.staff_report_item.lore", vars);
        String title = locale.getMessage("menus.staff_report_item.title", vars);
        CirrusItemType itemType = ReportRenderUtil.getReportItemType(report.type());

        return CirrusItem.of(
                itemType,
                CirrusChatElement.ofMiniMessage(title),
                MenuItems.lore(lore)
        );
    }

    @Override
    protected void handleClick(Click click, Report report) {
        if (report.id() == null) return;

        if (click.clickType().equals(CirrusClickType.RIGHT_CLICK))
            dismissReport(click, report);
        else
            inspectPlayer(click, report);
    }

    private void dismissReport(Click click, Report report) {
        sendMessage(MenuItems.COLOR_YELLOW + "Dismissing report...");

        httpClient.dismissReport(report.id(), viewerName, "Insufficient evidence").thenAccept(v -> {
            sendMessage(MenuItems.COLOR_GREEN + "Report dismissed.");

            StaffReportsMenu refreshed = new StaffReportsMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction)
                    .withFilter(currentFilter).withStatusFilter(currentStatusFilter);
            MenuAsync.displayWhenLoaded(platform, refreshed.getDataFuture(), click.player(), refreshed::display);
        }).exceptionally(e -> {
            sendMessage(MenuItems.COLOR_RED + "Failed to dismiss report: " + e.getMessage());
            return null;
        });
    }

    private void inspectPlayer(Click click, Report report) {
        if (report.reportedPlayerUuid() == null) {
            sendMessage(MenuItems.COLOR_RED + "Cannot inspect: no player UUID for this report.");
            return;
        }

        click.clickedMenu().close();

        httpClient.getPlayerProfile(report.reportedPlayerUuid()).thenAccept(response -> {
            if (response.getStatus() == 200) {
                new InspectMenu(platform, httpClient, viewerUuid, viewerName, response.getProfile(),
                    p -> {
                        StaffReportsMenu m = new StaffReportsMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, null);
                        MenuAsync.displayWhenLoaded(platform, m.getDataFuture(), p, m::display);
                    })
                    .display(click.player());
            } else {
                sendMessage(MenuItems.COLOR_RED + "Failed to load player profile");
            }
        }).exceptionally(e -> {
            sendMessage(MenuItems.COLOR_RED + "Failed to load player profile: " + e.getMessage());
            return null;
        });
    }

    @Override
    protected void registerActionHandlers() {
        super.registerActionHandlers();

        registerActionHandler("filter", this::handleFilter);

        registerActionHandler("openReports", click -> {});
    }

    private void handleFilter(Click click) {
        if (click.clickType().equals(CirrusClickType.RIGHT_CLICK)) {
            String newStatus = "open".equalsIgnoreCase(currentStatusFilter) ? "closed" : "open";
            StaffReportsMenu menu = new StaffReportsMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction)
                    .withFilter(currentFilter)
                    .withStatusFilter(newStatus);
            MenuAsync.displayWhenLoaded(platform, menu.getDataFuture(), click.player(), menu::display);
        } else {
            int currentIndex = filterOptions.indexOf(currentFilter);
            int nextIndex = (currentIndex + 1) % filterOptions.size();
            String newFilter = filterOptions.get(nextIndex);

            StaffReportsMenu menu = new StaffReportsMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction)
                    .withFilter(newFilter)
                    .withStatusFilter(currentStatusFilter);
            MenuAsync.displayWhenLoaded(platform, menu.getDataFuture(), click.player(), menu::display);
        }
    }
}

package gg.modl.minecraft.core.impl.menus.staff;

import gg.modl.minecraft.core.PluginServices;
import dev.simplix.cirrus.actionhandler.ActionHandlers;
import dev.simplix.cirrus.item.CirrusItem;
import dev.simplix.cirrus.item.CirrusItemType;
import dev.simplix.cirrus.model.Click;
import dev.simplix.cirrus.player.CirrusPlayerWrapper;
import dev.simplix.cirrus.text.CirrusChatElement;
import gg.modl.minecraft.api.http.ModlHttpClient;
import gg.modl.minecraft.api.http.response.RolesListResponse;
import gg.modl.minecraft.core.Platform;
import gg.modl.minecraft.core.cache.Cache;
import gg.modl.minecraft.core.impl.menus.base.BaseStaffListMenu;
import gg.modl.minecraft.core.impl.menus.util.MenuItems;
import gg.modl.minecraft.core.impl.menus.util.MenuAsync;
import gg.modl.minecraft.core.impl.menus.util.StaffTabItems.StaffTab;
import gg.modl.minecraft.core.util.Permissions;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RoleListMenu extends BaseStaffListMenu<RoleListMenu.Role> {
    public record Role(String id, String name, String description, List<String> permissions) {

    }

    private final List<Role> roles = new ArrayList<>();
    private final boolean hasPermission;
    @Getter private final CompletableFuture<Void> dataFuture;

    public RoleListMenu(Platform platform, ModlHttpClient httpClient, UUID viewerUuid, String viewerName,
                        boolean isAdmin, String panelUrl, Consumer<CirrusPlayerWrapper> backAction) {
        super("Edit Roles", platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction);
        activeTab = StaffTab.SETTINGS;

        Cache cache = PluginServices.cache();
        this.hasPermission = cache != null && cache.hasPermission(viewerUuid, Permissions.SETTINGS_MODIFY);

        if (hasPermission)
            this.dataFuture = fetchRoles();
        else
            this.dataFuture = CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> fetchRoles() {
        return httpClient.getRoles().thenAccept(response -> {
            if (response != null && response.getRoles() != null) {
                roles.clear();
                for (RolesListResponse.RoleEntry entry : response.getRoles()) {
                    if (entry.getId() != null && entry.getId().contains("super-admin")) {
                        continue;
                    }
                    roles.add(new Role(
                            entry.getId(),
                            entry.getName(),
                            entry.getDescription(),
                            entry.getPermissions() != null ? entry.getPermissions() : Collections.emptyList()
                    ));
                }
            }
        }).exceptionally(e -> null);
    }

    @Override
    protected Collection<Role> elements() {
        if (!hasPermission)
            return Collections.singletonList(new Role("no_permission", null, null, Collections.emptyList()));
        if (roles.isEmpty())
            return Collections.singletonList(new Role(null, null, null, Collections.emptyList()));
        return roles;
    }

    @Override
    protected CirrusItem map(Role role) {
        if ("no_permission".equals(role.id())) {
            return CirrusItem.of(
                    CirrusItemType.BARRIER,
                    CirrusChatElement.ofMiniMessage(MenuItems.COLOR_RED + "No Permission"),
                    MenuItems.lore(
                            MenuItems.COLOR_GRAY + "You don't have permission",
                            MenuItems.COLOR_GRAY + "to edit roles"
                    )
            );
        }
        if (role.id() == null) return createEmptyPlaceholder("No roles");

        List<String> lore = new ArrayList<>();

        if (role.description() != null && !role.description().isEmpty()) {
            lore.add(MenuItems.COLOR_WHITE + role.description());
            lore.add("");
        }

        lore.add(MenuItems.COLOR_GRAY + "Permissions:");
        if (role.permissions().isEmpty())
            lore.add(MenuItems.COLOR_DARK_GRAY + "  (No permissions)");
        else
            for (String perm : role.permissions())
                lore.add(MenuItems.COLOR_WHITE + "  - " + perm);
        lore.add("");
        lore.add(MenuItems.COLOR_YELLOW + "Click to edit permissions");

        return CirrusItem.of(
            CirrusItemType.PAPER,
            CirrusChatElement.ofMiniMessage(MenuItems.COLOR_GOLD + role.name()),
                MenuItems.lore(lore)
        );
    }

    @Override
    protected void handleClick(Click click, Role role) {
        if (role.id() == null || "no_permission".equals(role.id()) || !hasPermission) {
            return;
        }

        Cache cache = PluginServices.cache();
        if (cache != null) {
            String viewerRole = cache.getStaffRole(viewerUuid);
            if (viewerRole != null && viewerRole.equals(role.name())) {
                sendMessage(MenuItems.COLOR_RED + "You cannot edit your own role.");
                return;
            }
        }

        ActionHandlers.openMenu(
                new RolePermissionEditMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, role,
                        player -> {
                            RoleListMenu m = new RoleListMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction);
                            MenuAsync.displayWhenLoaded(platform, m.getDataFuture(), player, m::display);
                        }))
                .handle(click);
    }
}

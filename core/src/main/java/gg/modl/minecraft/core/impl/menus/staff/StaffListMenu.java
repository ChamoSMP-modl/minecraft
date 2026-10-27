package gg.modl.minecraft.core.impl.menus.staff;

import gg.modl.minecraft.core.PluginServices;
import dev.simplix.cirrus.actionhandler.ActionHandlers;
import dev.simplix.cirrus.item.CirrusItem;
import dev.simplix.cirrus.item.CirrusItemType;
import dev.simplix.cirrus.model.CirrusClickType;
import dev.simplix.cirrus.model.Click;
import dev.simplix.cirrus.player.CirrusPlayerWrapper;
import dev.simplix.cirrus.text.CirrusChatElement;
import gg.modl.minecraft.api.http.ModlHttpClient;
import gg.modl.minecraft.api.http.response.RolesListResponse;
import gg.modl.minecraft.api.http.response.StaffListResponse;
import gg.modl.minecraft.core.Platform;
import gg.modl.minecraft.core.cache.Cache;
import gg.modl.minecraft.core.impl.menus.base.BaseStaffListMenu;
import gg.modl.minecraft.core.impl.menus.util.MenuItems;
import gg.modl.minecraft.core.impl.menus.util.SkinTextureCache;
import gg.modl.minecraft.core.impl.menus.util.StaffTabItems.StaffTab;
import gg.modl.minecraft.core.util.Permissions;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class StaffListMenu extends BaseStaffListMenu<StaffListMenu.StaffMember> {
    public record StaffMember(String id, UUID uuid, String username, String currentRole) {

    }

    private static final String SUPER_ADMIN_ROLE = "Super Admin";

    private List<StaffMember> staffMembers = new ArrayList<>();
    private List<String> availableRoles = new ArrayList<>();
    private Map<String, Integer> roleOrders = new HashMap<>();
    private String viewerRole;
    private final Map<String, String> selectedRoles = new HashMap<>();
    private final boolean hasPermission;
    @Getter private final CompletableFuture<Void> dataFuture;

    public StaffListMenu(Platform platform, ModlHttpClient httpClient, UUID viewerUuid, String viewerName,
                         boolean isAdmin, String panelUrl, Consumer<CirrusPlayerWrapper> backAction) {
        super("Manage Staff", platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction);
        activeTab = StaffTab.SETTINGS;

        Cache cache = PluginServices.cache();
        this.hasPermission = cache != null && cache.hasPermission(viewerUuid, Permissions.STAFF_MANAGE);

        if (hasPermission)
            this.dataFuture = fetchStaffAndRoles();
        else
            this.dataFuture = CompletableFuture.completedFuture(null);
    }

    private StaffListMenu(Platform platform, ModlHttpClient httpClient, UUID viewerUuid, String viewerName,
                          boolean isAdmin, String panelUrl, Consumer<CirrusPlayerWrapper> backAction,
                          List<StaffMember> existingStaff, List<String> existingRoles,
                          Map<String, Integer> existingRoleOrders, String viewerRole,
                          Map<String, String> existingSelections) {
        super("Manage Staff", platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction);
        activeTab = StaffTab.SETTINGS;

        Cache cache = PluginServices.cache();
        this.hasPermission = cache != null && cache.hasPermission(viewerUuid, Permissions.STAFF_MANAGE);

        if (existingStaff != null) this.staffMembers = new ArrayList<>(existingStaff);
        if (existingRoles != null) this.availableRoles = new ArrayList<>(existingRoles);
        if (existingRoleOrders != null) this.roleOrders = new HashMap<>(existingRoleOrders);
        this.viewerRole = viewerRole;
        if (existingSelections != null) this.selectedRoles.putAll(existingSelections);
        this.dataFuture = CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> fetchStaffAndRoles() {
        CompletableFuture<Void> rolesFuture = httpClient.getRoles().thenAccept(response -> {
            if (response == null || !response.isSuccess() || response.getRoles() == null) {
                throw new IllegalStateException("Failed to load staff roles");
            }

            availableRoles.clear();
            roleOrders.clear();
            for (RolesListResponse.RoleEntry role : response.getRoles()) {
                roleOrders.put(role.getName(), role.getOrder());
                if (!SUPER_ADMIN_ROLE.equals(role.getName()))
                    availableRoles.add(role.getName());
            }
        });

        CompletableFuture<Void> staffFuture = httpClient.getStaffList().thenAccept(response -> {
            if (response == null || !response.isSuccess() || response.getStaff() == null) {
                throw new IllegalStateException("Failed to load staff members");
            }

            staffMembers.clear();
            for (StaffListResponse.StaffEntry entry : response.getStaff()) {
                UUID uuid = null;
                if (entry.getMinecraftUuid() != null) {
                    try {
                        uuid = UUID.fromString(entry.getMinecraftUuid());
                    } catch (Exception ignored) {}
                }
                String displayName = entry.getMinecraftUsername() != null ? entry.getMinecraftUsername() : entry.getUsername();
                staffMembers.add(new StaffMember(entry.getId(), uuid, displayName, entry.getRole()));

                if (uuid != null && uuid.equals(viewerUuid)) {
                    viewerRole = entry.getRole();
                }
            }

            for (StaffMember staff : staffMembers) {
                SkinTextureCache.ensureCached(staff.uuid());
            }
        });

        return CompletableFuture.allOf(rolesFuture, staffFuture);
    }

    private int getRoleOrder(String roleName) {
        return roleOrders.getOrDefault(roleName, Integer.MAX_VALUE);
    }

    private boolean isSuperAdmin(StaffMember staff) {
        return SUPER_ADMIN_ROLE.equals(staff.currentRole());
    }

    private boolean isViewerSelf(StaffMember staff) {
        return staff.uuid() != null && staff.uuid().equals(viewerUuid);
    }

    private boolean canModify(StaffMember staff) {
        if (isSuperAdmin(staff)) return false;
        if (isViewerSelf(staff)) return false;

        boolean viewerIsSuperAdmin = isAdmin
                || (PluginServices.cache() != null && SUPER_ADMIN_ROLE.equalsIgnoreCase(PluginServices.cache().getStaffRole(viewerUuid)));
        if (!viewerIsSuperAdmin && viewerRole == null) return false;

        int viewerOrder = viewerIsSuperAdmin ? Integer.MIN_VALUE : getRoleOrder(viewerRole);
        int targetOrder = getRoleOrder(staff.currentRole());
        return viewerOrder < targetOrder;
    }

    @Override
    protected Collection<StaffMember> elements() {
        if (!hasPermission)
            return Collections.singletonList(new StaffMember("no_permission", null, null, null));
        if (staffMembers.isEmpty())
            return Collections.singletonList(new StaffMember(null, null, null, null));
        return staffMembers;
    }

    @Override
    protected CirrusItem map(StaffMember staff) {
        if ("no_permission".equals(staff.id())) {
            return CirrusItem.of(
                    CirrusItemType.BARRIER,
                    CirrusChatElement.ofMiniMessage(MenuItems.COLOR_RED + "No Permission"),
                    MenuItems.lore(
                            MenuItems.COLOR_GRAY + "You don't have permission",
                            MenuItems.COLOR_GRAY + "to manage staff members"
                    )
            );
        }
        if (staff.id() == null)
            return createEmptyPlaceholder("No staff members");

        boolean canMod = canModify(staff);
        List<String> lore = canMod ? buildSelectableStaffLore(staff) : buildLockedStaffLore(staff);

        CirrusItem headItem = CirrusItem.of(
                CirrusItemType.PLAYER_HEAD,
                CirrusChatElement.ofMiniMessage(
                        (canMod ? MenuItems.COLOR_GOLD : MenuItems.COLOR_GRAY) + staff.username()),
                MenuItems.lore(lore)
        );

        headItem = SkinTextureCache.applyCachedOrFetch(headItem, staff.uuid());

        return headItem;
    }

    private List<String> buildLockedStaffLore(StaffMember staff) {
        List<String> lore = new ArrayList<>();
        lore.add(MenuItems.COLOR_GRAY + "Role: " + MenuItems.COLOR_WHITE + staff.currentRole());
        lore.add("");
        if (isSuperAdmin(staff)) {
            lore.add(MenuItems.COLOR_RED + "Super Admins cannot be modified");
        } else if (isViewerSelf(staff)) {
            lore.add(MenuItems.COLOR_RED + "You cannot modify your own role");
        } else {
            lore.add(MenuItems.COLOR_RED + "You cannot modify staff with");
            lore.add(MenuItems.COLOR_RED + "the same or higher rank");
        }
        return lore;
    }

    private List<String> buildSelectableStaffLore(StaffMember staff) {
        List<String> lore = new ArrayList<>();
        String selectedRole = selectedRoles.getOrDefault(staff.id(), staff.currentRole());

        lore.add(MenuItems.COLOR_GRAY + "Roles:");
        for (String role : availableRoles) {
            if (role.equals(staff.currentRole())) {
                lore.add(MenuItems.COLOR_GREEN + "  <b>" + role + " <reset><gray>(current)");
            } else if (role.equals(selectedRole) && !role.equals(staff.currentRole())) {
                lore.add(MenuItems.COLOR_GREEN + "  " + role + " <gray>(selected)");
            } else {
                lore.add(MenuItems.COLOR_GRAY + "  " + role);
            }
        }
        lore.add("");
        lore.add(MenuItems.COLOR_YELLOW + "Right-click to cycle roles");
        lore.add(MenuItems.COLOR_YELLOW + "Left-click to apply selected role");
        return lore;
    }

    @Override
    protected void handleClick(Click click, StaffMember staff) {
        if (staff.id() == null || "no_permission".equals(staff.id()) || !hasPermission) {
            return;
        }

        if (!canModify(staff)) {
            return;
        }

        if (click.clickType().equals(CirrusClickType.RIGHT_CLICK))
            cycleRole(click, staff);
        else
            applyRole(click, staff);
    }

    private void cycleRole(Click click, StaffMember staff) {
        if (availableRoles.isEmpty()) {
            sendMessage(MenuItems.COLOR_RED + "No roles available.");
            return;
        }

        String currentSelected = selectedRoles.getOrDefault(staff.id(), staff.currentRole());
        int currentIndex = availableRoles.indexOf(currentSelected);
        int nextIndex = (currentIndex + 1) % availableRoles.size();
        String nextRole = availableRoles.get(nextIndex);

        selectedRoles.put(staff.id(), nextRole);
        sendMessage(MenuItems.COLOR_YELLOW + "Selected role: " + MenuItems.COLOR_GREEN + nextRole +
                MenuItems.COLOR_GRAY + " (left-click to apply)");

        StaffListMenu newMenu = new StaffListMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl,
                backAction, staffMembers, availableRoles, roleOrders, viewerRole, selectedRoles);
        ActionHandlers.openMenu(newMenu).handle(click);
    }

    private void applyRole(Click click, StaffMember staff) {
        String selectedRole = selectedRoles.getOrDefault(staff.id(), staff.currentRole());

        if (selectedRole.equals(staff.currentRole())) {
            sendMessage(MenuItems.COLOR_GRAY + "This is already " + staff.username() + "'s current role.");
            return;
        }

        sendMessage(MenuItems.COLOR_YELLOW + "Applying role " + MenuItems.COLOR_GREEN + selectedRole +
                MenuItems.COLOR_YELLOW + " to " + staff.username() + "...");

        String actingStaffId = PluginServices.cache() != null ? PluginServices.cache().getStaffId(viewerUuid) : null;
        httpClient.updateStaffRole(staff.id(), selectedRole, actingStaffId).thenAccept(v -> {
            sendMessage(MenuItems.COLOR_GREEN + "Role updated successfully!");
            selectedRoles.remove(staff.id());

            httpClient.getStaffPermissions().exceptionally(e -> null);

            StaffListMenu newMenu = new StaffListMenu(platform, httpClient, viewerUuid, viewerName, isAdmin, panelUrl, backAction);
            ActionHandlers.openMenu(newMenu).handle(click);
        }).exceptionally(e -> {
            sendMessage(MenuItems.COLOR_RED + "Failed to update role: " + e.getMessage());
            return null;
        });
    }
}

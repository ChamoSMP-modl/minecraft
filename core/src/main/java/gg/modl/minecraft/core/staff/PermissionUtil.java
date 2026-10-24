package gg.modl.minecraft.core.staff;

import gg.modl.minecraft.api.http.response.SyncResponse;
import gg.modl.minecraft.core.Platform;
import gg.modl.minecraft.core.cache.Cache;
import gg.modl.minecraft.core.cache.CachedProfile;
import gg.modl.minecraft.core.util.CommandUtil;
import revxrsal.commands.command.CommandActor;

import java.util.UUID;

public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean hasPermission(CommandActor actor, Cache cache, String permission) {
        if (CommandUtil.isConsole(actor)) return true;
        int permIndex = permission.indexOf("modl.");
        if (permIndex == -1) permIndex = 0;
        return cache.hasPermission(actor.uniqueId(), permission.substring(permIndex));
    }

    public static boolean staffOrPermission(CommandActor actor, Cache cache, String permission, Platform platform, boolean needsPermissionOnStaff) {
        if (CommandUtil.isConsole(actor)) return true;
        int permIndex = permission.indexOf("modl.");
        if (permIndex == -1) permIndex = 0;
        if (needsPermissionOnStaff) {
            return (cache.hasPermission(actor.uniqueId(), permission.substring(permIndex)) && isStaff(actor, cache)) || platform.hasPermission(actor, permission);
        } else {
            return isStaff(actor, cache) || platform.hasPermission(actor, permission);
        }
    }

    public static boolean hasAnyPermission(CommandActor actor, Cache cache, Platform platform, String... permissions) {
        if (CommandUtil.isConsole(actor)) return true;
        for (String permission : permissions) {
            if (hasPermission(actor, cache, permission)) return true;
        }
        return false;
    }

    public static SyncResponse.ActiveStaffMember getStaffMember(CommandActor actor, Cache cache) {
        if (CommandUtil.isConsole(actor)) return null;
        CachedProfile profile = cache.getPlayerProfile(actor.uniqueId());
        return profile != null ? profile.getStaffMember() : null;
    }

    public static boolean isStaff(CommandActor actor, Cache cache) {
        if (CommandUtil.isConsole(actor)) return false;
        return isStaff(actor.uniqueId(), cache);
    }

    public static boolean isStaff(UUID playerUuid, Cache cache) {
        if (cache == null) return false;
        if (cache.isStaffMemberByPermissions(playerUuid)) return true;
        CachedProfile profile = cache.getPlayerProfile(playerUuid);
        return profile != null && profile.getStaffMember() != null;
    }

    public static String formatPunishmentPermission(String punishmentTypeName) {
        return "punishment.apply." + punishmentTypeName.toLowerCase().replace(" ", "-");
    }
}


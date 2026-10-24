package gg.modl.minecraft.api;

import java.util.UUID;

public record AbstractPlayer(UUID uuid, String username, String ipAddress, boolean online) {
    public AbstractPlayer(UUID uuid, String username, boolean online) {
        this(uuid, username, null, online);
    }

    public String getName() {
        return username;
    }
}

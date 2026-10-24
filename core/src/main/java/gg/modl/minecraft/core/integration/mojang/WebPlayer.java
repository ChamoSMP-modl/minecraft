package gg.modl.minecraft.core.integration.mojang;

import java.util.UUID;

public record WebPlayer(String name, UUID uuid, String skin, String textureValue, boolean valid) {
    private static final WebPlayer INVALID = new WebPlayer(null, null, null, null, false);

    public static WebPlayer invalid() {
        return INVALID;
    }
}

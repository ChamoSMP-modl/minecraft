package gg.modl.minecraft.velocity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Colors {
    private Colors() {
    }

    public static Component get(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }
}
package gg.modl.minecraft.spigot.bridge.reporter.hook;

import java.util.UUID;

public record AnticheatFlag(UUID uuid, String playerName, String checkName, String verbose) {
}

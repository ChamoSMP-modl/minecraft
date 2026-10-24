package gg.modl.minecraft.api.http.request;

public record StartupRequest(String pluginVersion, String platformType, String serverVersion, int maxPlayers) {
}

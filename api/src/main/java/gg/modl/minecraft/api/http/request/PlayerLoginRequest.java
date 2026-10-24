package gg.modl.minecraft.api.http.request;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record PlayerLoginRequest(@SerializedName("minecraftUUID") @NotNull String minecraftUuid,
                                 @NotNull String username, @SerializedName("ip") @Nullable String ipAddress,
                                 @Nullable String skinHash, @Nullable String serverName,
                                 @Nullable Map<String, Object> ipInfo, @Nullable String serverInstanceId) {
}

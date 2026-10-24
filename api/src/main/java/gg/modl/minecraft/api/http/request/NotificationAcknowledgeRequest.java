package gg.modl.minecraft.api.http.request;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record NotificationAcknowledgeRequest(@NotNull String playerUuid, @NotNull String acknowledgedAt,
                                             @NotNull List<String> notificationIds) {
}

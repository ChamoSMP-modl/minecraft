package gg.modl.minecraft.api.http.request;

import gg.modl.minecraft.api.http.ChatLogEntry;

import java.util.List;

public record ChatLogBatchRequest(List<ChatLogEntry> entries) {
}

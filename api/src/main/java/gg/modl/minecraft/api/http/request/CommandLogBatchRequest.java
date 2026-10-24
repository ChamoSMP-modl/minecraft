package gg.modl.minecraft.api.http.request;

import gg.modl.minecraft.api.http.CommandLogEntry;

import java.util.List;

public record CommandLogBatchRequest(List<CommandLogEntry> entries) {
}

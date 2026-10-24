package gg.modl.minecraft.core;

import gg.modl.minecraft.api.http.ModlHttpClient;
import org.jetbrains.annotations.NotNull;

public record HttpClientHolder(@NotNull ModlHttpClient client) {
}

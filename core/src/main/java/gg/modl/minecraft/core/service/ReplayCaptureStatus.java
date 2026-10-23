package gg.modl.minecraft.core.service;

import java.util.Locale;
public enum ReplayCaptureStatus {
    OK,
    NO_BRIDGE_CONNECTED,
    NO_ACTIVE_RECORDING,
    NOT_LOCAL,
    ERROR;

    public static ReplayCaptureStatus fromWire(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return ReplayCaptureStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return ERROR;
        }
    }
}

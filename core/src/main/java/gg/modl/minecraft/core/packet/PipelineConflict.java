package gg.modl.minecraft.core.packet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record PipelineConflict(String pluginId, String displayName, String remediation) {
    private static final List<PipelineConflict> KNOWN = Collections.unmodifiableList(Arrays.asList(
            new PipelineConflict("limboapi", "LimboAPI",
                    "set 'compatibility-mode: true' and 'save-uncompressed-packets: true' in LimboAPI's config"),
            new PipelineConflict("eaglerxserver", "EaglerXServer",
                    "run EaglerXServer on a dedicated proxy that does not host modl's proxy menus")
    ));

    public static List<PipelineConflict> known() {
        return KNOWN;
    }
}

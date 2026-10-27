package gg.modl.minecraft.bridge.config;

import gg.modl.minecraft.core.util.PluginLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffModeConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsConfiguredHotbarAndScoreboardValues() throws IOException {
        writeStaffModeConfig(
                "vanish_on_enable: false",
                "staff_hotbar:",
                "  2:",
                "    item: \"minecraft:stick\"",
                "    name: \"<green>Inspect\"",
                "    action: \"target_selector\"",
                "    lore:",
                "      - \"<gray>Line one\"",
                "target_hotbar:",
                "  5:",
                "    item: \"minecraft:book\"",
                "    name: \"<yellow>Notes\"",
                "    action: \"inspect_target\"",
                "staff_scoreboard:",
                "  enabled: true",
                "  title: \"<aqua>Staff\"",
                "  vanish: \" <gray>[V]\"",
                "  lines:",
                "    - \"<white>Players: <green>{online}\""
        );

        StaffModeConfig config = StaffModeConfig.load(tempDir, PluginLogger.fromJul(Logger.getLogger("test")));

        assertFalse(config.isVanishOnEnable());
        assertEquals("minecraft:stick", config.getStaffHotbar().get(2).getItem());
        assertEquals("<green>Inspect", config.getStaffHotbar().get(2).getName());
        assertEquals("target_selector", config.getStaffHotbar().get(2).getAction());
        assertEquals("<gray>Line one", config.getStaffHotbar().get(2).getLore().get(0));
        assertEquals("minecraft:book", config.getTargetHotbar().get(5).getItem());
        assertTrue(config.getStaffScoreboard().isEnabled());
        assertEquals("<aqua>Staff", config.getStaffScoreboard().getTitle());
    }

    @Test
    void usesDefaultsWhenFileIsMissing() {
        StaffModeConfig config = StaffModeConfig.load(tempDir, PluginLogger.fromJul(Logger.getLogger("test")));

        assertTrue(config.isVanishOnEnable());
        assertEquals("minecraft:lead", config.getStaffHotbar().get(0).getItem());
        assertEquals("target_selector", config.getStaffHotbar().get(0).getAction());
        assertEquals("minecraft:ice", config.getTargetHotbar().get(0).getItem());
        assertEquals("freeze_target", config.getTargetHotbar().get(0).getAction());
    }

    @Test
    void usesDefaultsWhenFileIsMalformed() throws IOException {
        writeStaffModeConfig(
                "staff_hotbar:",
                "  0:",
                "    item: ["
        );

        StaffModeConfig config = StaffModeConfig.load(tempDir, PluginLogger.fromJul(Logger.getLogger("test")));

        assertTrue(config.isVanishOnEnable());
        assertEquals("minecraft:lead", config.getStaffHotbar().get(0).getItem());
        assertEquals("minecraft:ice", config.getTargetHotbar().get(0).getItem());
    }

    private void writeStaffModeConfig(String... lines) throws IOException {
        Files.write(tempDir.resolve("staff_mode.yml"),
                String.join(System.lineSeparator(), lines).getBytes(StandardCharsets.UTF_8));
    }
}

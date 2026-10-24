package gg.modl.minecraft.core.config.yaml;

import gg.modl.minecraft.core.util.PluginLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUpdaterTest {

    private static final PluginLogger LOGGER = new PluginLogger() {
        @Override public void info(String message) {}
        @Override public void warning(String message) {}
        @Override public void severe(String message) {}
    };

    @TempDir
    Path dataFolder;

    @Test
    void createsFileFromPackagedDefaultWhenMissing() {
        ConfigUpdate update = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertTrue(update.isCreated());
        assertTrue(Files.exists(dataFolder.resolve("config.yml")));
    }

    @Test
    void reportsNothingToDoForAnUntouchedDefault() {
        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        ConfigUpdate second = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertFalse(second.isChanged());
    }

    @Test
    void addsMissingTopLevelKeyWithItsComments() throws IOException {
        write("config.yml", "# Language for in-game messages\nlocale: \"en_US\"\n");

        ConfigUpdate update = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        String result = read("config.yml");
        assertTrue(update.getAddedPaths().contains("debug"));
        assertTrue(result.contains("locale: \"en_US\""));
        assertTrue(result.contains("# Enable this if developer tells you - will spam console logs"));
        assertTrue(result.contains("debug: false"));
    }

    @Test
    void keepsExistingValuesAndCommentsUntouched() throws IOException {
        write("config.yml", "# my own note\nlocale: \"de_DE\"\ndebug: true\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        String result = read("config.yml");
        assertTrue(result.contains("# my own note"));
        assertTrue(result.contains("locale: \"de_DE\""));
        assertTrue(result.contains("debug: true"));
        assertFalse(result.contains("debug: false"));
    }

    @Test
    void insertsNestedKeyInsideItsParentSection() throws IOException {
        write("config.yml", "server:\n  name: \"Mine\"\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertEquals(false, valueAt("config.yml", "server", "query_mojang"));
        assertEquals("Mine", valueAt("config.yml", "server", "name"));
    }

    @Test
    void neverTouchesAFullyDynamicFile() throws IOException {
        String trimmed = "1:\n  enabled: false\n";
        write("punish_gui.yml", trimmed);

        ConfigUpdate update = ConfigUpdater.update(CoreManagedConfigs.PUNISH_GUI, dataFolder, LOGGER);

        assertFalse(update.isChanged());
        assertEquals(trimmed, read("punish_gui.yml"));
    }

    @Test
    void doesNotResurrectEntriesDeletedFromADynamicSection() throws IOException {
        write("updater-dynamic.yml", "enabled: true\nslots:\n  0:\n    item: \"minecraft:lead\"\n");

        ConfigUpdater.update(dynamicFixture(), dataFolder, LOGGER);

        Map<String, Object> slots = section("updater-dynamic.yml", "slots");
        assertEquals(1, slots.size());
        assertTrue(slots.containsKey(0));
    }

    @Test
    void doesNotAddFieldsToEntriesInsideADynamicSection() throws IOException {
        write("updater-dynamic.yml", "enabled: true\nslots:\n  0:\n    item: \"minecraft:lead\"\n");

        ConfigUpdater.update(dynamicFixture(), dataFolder, LOGGER);

        Map<?, ?> slot = (Map<?, ?>) section("updater-dynamic.yml", "slots").get(0);
        assertEquals(1, slot.size());
        assertFalse(slot.containsKey("lore"));
    }

    @Test
    void stillAddsAMissingDynamicSectionWholesale() throws IOException {
        write("updater-dynamic.yml", "enabled: true\n");

        ConfigUpdater.update(dynamicFixture(), dataFolder, LOGGER);

        assertEquals(2, section("updater-dynamic.yml", "slots").size());
    }

    @Test
    void addsMissingKeysToFixedSectionsBesideADynamicOne() throws IOException {
        write("updater-dynamic.yml", "enabled: true\nslots:\n  0:\n    item: \"x\"\ndisplay:\n  title: \"Mine\"\n");

        ConfigUpdater.update(dynamicFixture(), dataFolder, LOGGER);

        assertEquals(true, valueAt("updater-dynamic.yml", "display", "enabled"));
        assertEquals("Mine", valueAt("updater-dynamic.yml", "display", "title"));
    }

    @Test
    void doesNotMergeIntoAUserShortenedList() throws IOException {
        write("updater-dynamic.yml", "display:\n  lines:\n    - \"only mine\"\n");

        ConfigUpdater.update(dynamicFixture(), dataFolder, LOGGER);

        Map<String, Object> display = section("updater-dynamic.yml", "display");
        assertEquals(1, ((java.util.List<?>) display.get("lines")).size());
    }

    @Test
    void insertsAMissingKeyInPackagedOrderNotAtTheEnd() throws IOException {
        write("updater-dynamic.yml", "enabled: true\nfooter: \"bye\"\n");

        ConfigUpdater.update(dynamicFixture(), dataFolder, LOGGER);

        String result = read("updater-dynamic.yml");
        assertTrue(result.indexOf("slots:") > result.indexOf("enabled:"));
        assertTrue(result.indexOf("slots:") < result.indexOf("footer:"));
    }

    @Test
    void leavesUserEditedListsAlone() throws IOException {
        write("config.yml", "muted_commands:\n  - \"msg\"\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        Yaml yaml = new Yaml();
        Map<?, ?> loaded = yaml.load(read("config.yml"));
        assertEquals(1, ((java.util.List<?>) loaded.get("muted_commands")).size());
    }

    @Test
    void keepsUnknownUserKeys() throws IOException {
        write("config.yml", "locale: \"en_US\"\nmy_custom_key: 42\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        Yaml yaml = new Yaml();
        Map<?, ?> loaded = yaml.load(read("config.yml"));
        assertEquals(42, loaded.get("my_custom_key"));
    }

    @Test
    void leavesAnUnparseableFileUntouched() throws IOException {
        String broken = "locale: \"en_US\n  bad: [";
        write("config.yml", broken);

        ConfigUpdate update = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertFalse(update.isChanged());
        assertEquals(broken, read("config.yml"));
    }

    @Test
    void writesABackupBeforeChangingAFile() throws IOException {
        write("config.yml", "locale: \"en_US\"\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertTrue(Files.exists(dataFolder.resolve("config.yml.bak")));
        assertEquals("locale: \"en_US\"\n", read("config.yml.bak"));
    }

    @Test
    void handlesWindowsLineEndings() throws IOException {
        write("config.yml", "locale: \"en_US\"\r\ndebug: true\r\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        Yaml yaml = new Yaml();
        Map<?, ?> loaded = yaml.load(read("config.yml"));
        assertEquals(true, loaded.get("debug"));
        assertEquals("en_US", loaded.get("locale"));
    }

    @Test
    void reindentsInsertedChildrenToMatchTheUserFile() throws IOException {
        write("config.yml", "server:\n    name: \"Mine\"\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertEquals(false, valueAt("config.yml", "server", "query_mojang"));
        assertTrue(read("config.yml").contains("    query_mojang: false"));
    }

    @Test
    void everyPackagedDefaultSurvivesARoundTrip() throws IOException {
        for (ManagedConfig config : new ManagedConfig[]{
                CoreManagedConfigs.CONFIG, CoreManagedConfigs.BOOT,
                CoreManagedConfigs.PUNISH_GUI, CoreManagedConfigs.REPORT_GUI,
                CoreManagedConfigs.locale("en_US")}) {
            ConfigUpdater.update(config, dataFolder, LOGGER);
            ConfigUpdate second = ConfigUpdater.update(config, dataFolder, LOGGER);
            assertFalse(second.isChanged(), config.getFileName() + " was changed on a second pass");
        }
    }

    @Test
    void refusesToTouchAFlowMappingParent() throws IOException {
        String original = "server: {name: \"x\"}\n";
        write("config.yml", original);

        ConfigUpdate update = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertFalse(update.isChanged());
        assertEquals(original, read("config.yml"));
    }

    @Test
    void refusesToTouchAFileWithDuplicateKeys() throws IOException {
        String original = "locale: \"en_US\"\nserver:\n  name: \"a\"\nserver:\n  name: \"b\"\n";
        write("config.yml", original);

        ConfigUpdate update = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertFalse(update.isChanged());
        assertEquals(original, read("config.yml"));
    }

    @Test
    void refusesToTouchAnAliasedParent() throws IOException {
        String original = "base: &b\n  name: \"x\"\nserver: *b\n";
        write("config.yml", original);

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertParses("config.yml");
    }

    @Test
    void fillsInChildrenOfAParentTheOwnerEmptied() throws IOException {
        write("config.yml", "realtime:\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertEquals(true, valueAt("config.yml", "realtime", "enabled"));
    }

    @Test
    void isIdempotentOnAHandEditedFile() throws IOException {
        write("config.yml", "# mine\nlocale: \"de_DE\"\nserver:\n  name: \"Mine\"\n");

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);
        String afterFirst = read("config.yml");
        ConfigUpdate second = ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertFalse(second.isChanged());
        assertEquals(afterFirst, read("config.yml"));
    }

    @Test
    void keepsTheOriginalBackupAcrossRepeatedUpgrades() throws IOException {
        String original = "locale: \"en_US\"\n";
        write("config.yml", original);

        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);
        Files.writeString(dataFolder.resolve("config.yml"),
                "locale: \"en_US\"\n");
        ConfigUpdater.update(CoreManagedConfigs.CONFIG, dataFolder, LOGGER);

        assertEquals(original, read("config.yml.bak"));
    }

    private void assertParses(String name) throws IOException {
        new Yaml().load(read(name));
    }

    private static ManagedConfig dynamicFixture() {
        return ManagedConfig.of("updater-dynamic.yml", ConfigSchema.withDynamicSections("slots"));
    }

    private void write(String name, String content) throws IOException {
        Path file = dataFolder.resolve(name);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private String read(String name) throws IOException {
        return new String(Files.readAllBytes(dataFolder.resolve(name)), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> section(String file, String key) throws IOException {
        Map<String, Object> loaded = new Yaml().load(read(file));
        return (Map<String, Object>) loaded.get(key);
    }

    private Object valueAt(String file, String parent, String key) throws IOException {
        return section(file, parent).get(key);
    }
}

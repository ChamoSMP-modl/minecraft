package gg.modl.minecraft.spigot;

import com.alessiodp.libby.PaperLibraryManager;
import com.alessiodp.libby.logging.LogLevel;
import gg.modl.minecraft.api.LibraryRecord;
import gg.modl.minecraft.core.Libraries;
import gg.modl.minecraft.core.boot.LibraryLoader;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.plugin.Plugin;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
public class PaperLoader implements PluginLoader {

    private static final MavenLibraryResolver resolver = new MavenLibraryResolver();

    public static void resolveLibbyDependencies(Plugin plugin) {
        PaperLibraryManager libraryManager = new PaperLibraryManager(plugin);
        libraryManager.setLogLevel(LogLevel.WARN);
        libraryManager.addMavenCentral();
        libraryManager.addRepository("https://nexus.modl.gg/repository/maven-releases/");

        for (LibraryRecord record : Libraries.PROTO_DEPS_RELOCATED) {
            libraryManager.loadLibrary(LibraryLoader.toLibrary(record));
        }
    }

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());

        for (LibraryRecord record : Libraries.COMMON) loadLibrary(record);

        loadLibrary(Libraries.LAMP_BUKKIT);

        classpathBuilder.addLibrary(resolver);
    }

    private void loadLibrary(LibraryRecord record) {
        resolver.addDependency(new Dependency(new DefaultArtifact(
                        String.format("%s:%s:%s", record.groupId().replace("{}", "."), record.artifactId(), record.version())
                ), null)
        );
    }
}

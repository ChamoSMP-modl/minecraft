package gg.modl.minecraft.spigot;

import gg.modl.minecraft.api.LibraryRecord;
import gg.modl.minecraft.core.Libraries;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
public class PaperLoader implements PluginLoader {

    private static final MavenLibraryResolver resolver = new MavenLibraryResolver();

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addRepository(new RemoteRepository.Builder("modl", "default", "https://nexus.modl.gg/repository/maven-releases/").build());
        resolver.addRepository(new RemoteRepository.Builder("codemc", "default", "https://repo.codemc.io/repository/maven-releases/").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());

        for (LibraryRecord record : Libraries.PROTO_DEPS_RELOCATED) loadLibrary(record);
        for (LibraryRecord record : Libraries.COMMON) loadLibrary(record);

        loadLibrary(Libraries.LAMP_COMMON);
        loadLibrary(Libraries.LAMP_BRIGADIER);
        loadLibrary(Libraries.LAMP_BUKKIT);
        loadLibrary(Libraries.CIRRUS_SPIGOT);

        classpathBuilder.addLibrary(resolver);
    }

    private void loadLibrary(LibraryRecord record) {
        resolver.addDependency(new Dependency(new DefaultArtifact(
                        record.groupId().replace("{}", ".") + ":" +
                                record.artifactId().replace("{}", ".") + ":" +
                                record.version().replace("{}", ".")
                ), null)
        );
    }
}

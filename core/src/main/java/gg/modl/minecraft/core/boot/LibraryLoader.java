package gg.modl.minecraft.core.boot;

import com.alessiodp.libby.Library;
import gg.modl.minecraft.api.LibraryRecord;
import org.jspecify.annotations.NonNull;

public final class LibraryLoader {
    private LibraryLoader() {
    }

    public static @NonNull Library toLibrary(@NonNull LibraryRecord record) {
        Library.Builder builder = Library.builder()
                .isolatedLoad(false)
                .groupId(record.groupId())
                .artifactId(record.artifactId())
                .version(record.version());

        if (record.hasRelocations()) {
            for (String[] relocation : record.relocations()) {
                builder.relocate(relocation[0], relocation[1]);
            }
        }
        if (record.url() != null) builder.url(record.url());
        if (record.hasChecksum()) builder.checksumFromBase64(record.checksum());

        return builder.build();
    }
}

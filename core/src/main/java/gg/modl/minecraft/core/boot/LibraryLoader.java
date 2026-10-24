package gg.modl.minecraft.core.boot;

import com.alessiodp.libby.Library;
import gg.modl.minecraft.api.LibraryRecord;

public final class LibraryLoader {
    private LibraryLoader() {
    }

    public static Library toLibrary(LibraryRecord record) {
        Library.Builder builder = Library.builder()
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

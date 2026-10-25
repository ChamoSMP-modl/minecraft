package gg.modl.minecraft.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import gg.modl.minecraft.api.LibraryRecord;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibrariesTest {

    @Test
    void runtimeLoadedLibrariesHaveChecksums() {
        runtimeLibraries().forEach(library -> assertTrue(library.hasChecksum(), library.id()));
    }

    private static List<LibraryRecord> runtimeLibraries() {
        return Arrays.stream(Libraries.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> LibraryRecord.class.equals(field.getType()))
                .map(LibrariesTest::readLibraryRecord)
                .collect(Collectors.toList());
    }

    private static LibraryRecord readLibraryRecord(Field field) {
        try {
            return (LibraryRecord) field.get(null);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to read library record " + field.getName(), e);
        }
    }
}

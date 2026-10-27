package gg.modl.minecraft.core;

import gg.modl.minecraft.api.LibraryRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Libraries {

    private static final String[][] PROTOBUF_RELOCATIONS = {
            {"com{}google{}protobuf", "gg{}modl{}libs{}protobuf"}
    };

    private static final String[][] CIRRUS_RELOCATIONS = {
            {"com.github.retrooper.packetevents", "com.github.retrooper.packetevents"}
    };

    public static final LibraryRecord HTTPCLIENT5 = LibraryRecord.of(
            "org{}apache{}httpcomponents{}client5",
            "httpclient5",
            LibraryVersions.HTTPCLIENT5,
            "httpclient5",
            LibraryVersions.HTTPCLIENT5_CHECKSUM
    ),
            HTTPCORE5 = LibraryRecord.of(
                    "org{}apache{}httpcomponents{}core5",
                    "httpcore5",
                    LibraryVersions.HTTPCORE5,
                    "httpcore5",
                    LibraryVersions.HTTPCORE5_CHECKSUM
            ),
            HTTPCORE5_H2 = LibraryRecord.of(
                    "org{}apache{}httpcomponents{}core5",
                    "httpcore5-h2",
                    LibraryVersions.HTTPCORE5_H2,
                    "httpcore5-h2",
                    LibraryVersions.HTTPCORE5_H2_CHECKSUM
            ),
            JAVA_WEBSOCKET = LibraryRecord.of(
                    "org{}java-websocket",
                    "Java-WebSocket",
                    LibraryVersions.JAVA_WEBSOCKET,
                    "Java-WebSocket",
                    LibraryVersions.JAVA_WEBSOCKET_CHECKSUM
            ),
            LAMP_COMMON = LibraryRecord.of(
                    "io{}github{}revxrsal",
                    "lamp.common",
                    LibraryVersions.LAMP_COMMON,
                    "lamp-common",
                    LibraryVersions.LAMP_COMMON_CHECKSUM
            ),
            LAMP_BRIGADIER = LibraryRecord.of(
                    "io{}github{}revxrsal",
                    "lamp.brigadier",
                    LibraryVersions.LAMP_BRIGADIER,
                    "lamp-brigadier",
                    LibraryVersions.LAMP_BRIGADIER_CHECKSUM
            ),
            LAMP_BUKKIT = LibraryRecord.of(
                    "io{}github{}revxrsal",
                    "lamp.bukkit",
                    LibraryVersions.LAMP_BUKKIT,
                    "lamp-bukkit",
                    LibraryVersions.LAMP_BUKKIT_CHECKSUM
            ),
            LAMP_VELOCITY = LibraryRecord.of(
                    "io{}github{}revxrsal",
                    "lamp.velocity",
                    LibraryVersions.LAMP_VELOCITY,
                    "lamp-velocity",
                    LibraryVersions.LAMP_VELOCITY_CHECKSUM
            ),
            CIRRUS_VELOCITY = LibraryRecord.of(
                    "net{}chamosmp{}cirrus",
                    "cirrus-velocity",
                    LibraryVersions.CIRRUS_VELOCITY,
                    "cirrus-velocity",
                    LibraryVersions.CIRRUS_VELOCITY_CHECKSUM,
                    CIRRUS_RELOCATIONS
            ),
            CIRRUS_API = LibraryRecord.of(
                    "net{}chamosmp{}cirrus",
                    "cirrus-api",
                    LibraryVersions.CIRRUS_API,
                    "cirrus-api",
                    LibraryVersions.CIRRUS_API_CHECKSUM,
                    CIRRUS_RELOCATIONS
            ),
            CIRRUS_COMMON = LibraryRecord.of(
                    "net{}chamosmp{}cirrus",
                    "cirrus-common",
                    LibraryVersions.CIRRUS_COMMON,
                    "cirrus-common",
                    LibraryVersions.CIRRUS_COMMON_CHECKSUM,
                    CIRRUS_RELOCATIONS
            ),
            PROTOBUF_JAVA = LibraryRecord.of(
                    "com{}google{}protobuf",
                    "protobuf-java",
                    LibraryVersions.PROTOBUF_JAVA,
                    "protobuf-java",
                    LibraryVersions.PROTOBUF_JAVA_CHECKSUM
            ),
            PROTOBUF_JAVA_UTIL = LibraryRecord.of(
                    "com{}google{}protobuf",
                    "protobuf-java-util",
                    LibraryVersions.PROTOBUF_JAVA_UTIL,
                    "protobuf-java-util",
                    LibraryVersions.PROTOBUF_JAVA_UTIL_CHECKSUM
            ),
            FAILUREACCESS = LibraryRecord.of(
                    "com{}google{}guava",
                    "failureaccess",
                    LibraryVersions.FAILUREACCESS,
                    "failureaccess",
                    LibraryVersions.FAILUREACCESS_CHECKSUM
            ),
            PROTOVALIDATE = LibraryRecord.of(
                    "build{}buf",
                    "protovalidate",
                    LibraryVersions.PROTOVALIDATE,
                    "protovalidate",
                    LibraryVersions.PROTOVALIDATE_CHECKSUM
            ),
            CEL_CORE = LibraryRecord.of(
                    "org{}projectnessie{}cel",
                    "cel-core",
                    LibraryVersions.CEL_CORE,
                    "cel-core",
                    LibraryVersions.CEL_CORE_CHECKSUM
            ),
            CEL_GENERATED_ANTLR = LibraryRecord.of(
                    "org{}projectnessie{}cel",
                    "cel-generated-antlr",
                    LibraryVersions.CEL_GENERATED_ANTLR,
                    "cel-generated-antlr",
                    LibraryVersions.CEL_GENERATED_ANTLR_CHECKSUM
            ),
            CEL_GENERATED_PB = LibraryRecord.of(
                    "org{}projectnessie{}cel",
                    "cel-generated-pb",
                    LibraryVersions.CEL_GENERATED_PB,
                    "cel-generated-pb",
                    LibraryVersions.CEL_GENERATED_PB_CHECKSUM
            ),
            AGRONA = LibraryRecord.of(
                    "org{}agrona",
                    "agrona",
                    LibraryVersions.AGRONA,
                    "agrona",
                    LibraryVersions.AGRONA_CHECKSUM
            ),
            IPADDRESS = LibraryRecord.of(
                    "com{}github{}seancfoley",
                    "ipaddress",
                    LibraryVersions.IPADDRESS,
                    "ipaddress",
                    LibraryVersions.IPADDRESS_CHECKSUM
            ),
            JAKARTA_MAIL_API = LibraryRecord.of(
                    "jakarta{}mail",
                    "jakarta.mail-api",
                    LibraryVersions.JAKARTA_MAIL_API,
                    "jakarta-mail-api",
                    LibraryVersions.JAKARTA_MAIL_API_CHECKSUM
            ),
            JAKARTA_ACTIVATION_API = LibraryRecord.of(
                    "jakarta{}activation",
                    "jakarta.activation-api",
                    LibraryVersions.JAKARTA_ACTIVATION_API,
                    "jakarta-activation-api",
                    LibraryVersions.JAKARTA_ACTIVATION_API_CHECKSUM
            ),


    MODL_PROTO = LibraryRecord.of(
            "gg{}modl",
            "proto",
            LibraryVersions.MODL_PROTO,
            "proto",
            LibraryVersions.MODL_PROTO_CHECKSUM
    );


    public static final List<LibraryRecord> COMMON = Arrays.asList(
            HTTPCLIENT5,
            HTTPCORE5,
            HTTPCORE5_H2,
            JAVA_WEBSOCKET,
            LAMP_BRIGADIER,
            LAMP_COMMON
    );

    public static final List<LibraryRecord> PROTO_DEPS = Arrays.asList(
            PROTOBUF_JAVA,
            PROTOBUF_JAVA_UTIL,
            FAILUREACCESS,
            PROTOVALIDATE,
            CEL_CORE,
            CEL_GENERATED_ANTLR,
            CEL_GENERATED_PB,
            AGRONA,
            IPADDRESS,
            JAKARTA_MAIL_API,
            JAKARTA_ACTIVATION_API,
            MODL_PROTO
    );


    public static final List<LibraryRecord> PROTO_DEPS_RELOCATED = relocateProtobuf();

    private static List<LibraryRecord> relocateProtobuf() {
        List<LibraryRecord> relocated = new ArrayList<>(Libraries.PROTO_DEPS.size());
        for (LibraryRecord dep : Libraries.PROTO_DEPS) {
            relocated.add(dep.withRelocations(mergeRelocations(dep.relocations())));
        }
        return Collections.unmodifiableList(relocated);
    }

    private static String[][] mergeRelocations(String[][] existing) {
        if (existing == null || existing.length == 0) return Libraries.PROTOBUF_RELOCATIONS;
        String[][] merged = Arrays.copyOf(existing, existing.length + Libraries.PROTOBUF_RELOCATIONS.length);
        System.arraycopy(Libraries.PROTOBUF_RELOCATIONS, 0, merged, existing.length, Libraries.PROTOBUF_RELOCATIONS.length);
        return merged;
    }
}

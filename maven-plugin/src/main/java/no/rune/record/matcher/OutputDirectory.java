package no.rune.record.matcher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.isDirectory;

record OutputDirectory(Path path) {

    boolean exists() {
        return isDirectory(path);
    }

    Path create() {
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to create output directory " + path + ", " +
                    "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

}

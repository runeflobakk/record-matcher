package no.rune.record.matcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.Files.isDirectory;

public record JavaCompilationUnit(String content, String name, Optional<String> packageName) {

    public JavaCompilationUnit(String content, String name, Package location) {
        this(content, name, Optional.of(location).map(Package::getName));
    }

    Path writeToBaseDirectory(Path baseDirectory) throws NotDirectoryException, IOException {
        if (!isDirectory(baseDirectory)) {
            throw new NotDirectoryException(baseDirectory.toString());
        }
        var targetDirectory = packageName.map(p -> baseDirectory.resolve(p.replace('.', '/'))).orElse(baseDirectory);
        var targetFile = targetDirectory.resolve(name + ".java");
        try {
            if (!baseDirectory.equals(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }
            return Files.writeString(targetFile, content);
        } catch (IOException e) {
            throw new IOException("Unable to write to " + targetFile + ", " +
                    "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}

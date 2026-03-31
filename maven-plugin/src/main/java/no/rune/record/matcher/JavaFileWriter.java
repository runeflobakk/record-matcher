package no.rune.record.matcher;

import no.rune.typecompanion.JavaCompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

import static java.nio.file.Files.isDirectory;

final class JavaFileWriter {

    private final Path baseDirectory;

    JavaFileWriter(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    Path writeToFile(JavaCompilationUnit compilationUnit) throws NotDirectoryException, IOException {
        if (!isDirectory(baseDirectory)) {
            throw new NotDirectoryException(baseDirectory.toString());
        }
        var targetDirectory = compilationUnit.packageName().map(p -> baseDirectory.resolve(p.replace('.', '/'))).orElse(baseDirectory);
        var targetFile = targetDirectory.resolve(compilationUnit.name() + ".java");
        try {
            if (!baseDirectory.equals(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }
            return Files.writeString(targetFile, compilationUnit.content());
        } catch (IOException e) {
            throw new IOException("Unable to write to " + targetFile + ", " +
                    "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}

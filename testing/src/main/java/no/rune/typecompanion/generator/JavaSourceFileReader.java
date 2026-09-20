package no.rune.typecompanion.generator;

import no.rune.maven.Maven;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static java.lang.reflect.Modifier.isPublic;
import static java.nio.file.Files.readString;

public final class JavaSourceFileReader {

    public static final JavaSourceFileReader testSourcesReader = new JavaSourceFileReader(Maven.javaTestSourceFiles);

    private final Path root;

    public JavaSourceFileReader(Path root) {
        this.root = root;
    }

    public String readSourceOf(Class<?> cls) {
        verifyIsPublicTopLevelClass(cls);
        Path sourceFile = root.resolve(cls.getName().replaceAll("\\.", "/") + ".java");
        try {
            return readString(sourceFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Error resolving contents of source file " +
                    "for " + cls + ", expected to be located at " + sourceFile + ", " +
                    e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static void verifyIsPublicTopLevelClass(Class<?> cls) {
        if (!isPublic(cls.getModifiers()) && cls.getDeclaringClass() != null) {
            throw new IllegalArgumentException(cls + " is not a public top-level class");
        }
    }
}

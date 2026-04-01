package no.rune.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static java.util.stream.Stream.iterate;

public final class Maven {

    public static final Path basedir; static {
        URI rootClassPathUri;
        try {
            rootClassPathUri = Maven.class.getClassLoader().getResource(".").toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Error resolving root classpath uri, " +
                    "because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
        basedir = iterate(Path.of(rootClassPathUri), path -> path.getParent())
            .takeWhile(path -> path.getParent() != null)
            .filter(path -> exists(path.resolve("pom.xml")))
            .map(Path::normalize)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to locate basedir containing pom.xml"));
    }

    public static final Path javaTestSourceFiles = basedir.resolve("src/test/java");


    private Maven() {
    }
}

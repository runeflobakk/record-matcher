package no.rune.record;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.readString;
import static no.rune.util.Maven.javaTestSourceFiles;

record ExpectedMatcher(String fullyQualifiedClassName, String sourceCode) {

    public static ExpectedMatcher expectedMatcherFor(Class<?> record) {
        if (!record.isRecord()) {
            throw new IllegalArgumentException("not a record: " + record);
        }
        String fullyQualifiedMatcherClassName = record.getName() + "Matcher";
        Path expectedMatcherSourceFile = javaTestSourceFiles.resolve(fullyQualifiedMatcherClassName.replaceAll("\\.", "/") + ".java");
        try {
            return new ExpectedMatcher(fullyQualifiedMatcherClassName, readString(expectedMatcherSourceFile));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error resolving " + expectedMatcherSourceFile + ", " +
                    "because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }

}

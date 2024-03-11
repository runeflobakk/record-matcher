package no.rune.record.matcher;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.testing.compile.Compilation.Status.SUCCESS;
import static com.google.testing.compile.Compiler.javac;
import static java.nio.file.Files.readString;
import static no.rune.util.Maven.javaTestSourceFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

public record ExpectedMatcher(String fullyQualifiedClassName, String sourceCode, Class<? extends Record> record) {

    public static ExpectedMatcher expectedMatcherFor(Class<? extends Record> record) {
        String fullyQualifiedMatcherClassName = record.getPackageName() + "." + RecordMatcherGenerator.DEFAULT_MATCHER_NAME_RESOLVER.resolve(record);
        Path expectedMatcherSourceFile = javaTestSourceFiles.resolve(fullyQualifiedMatcherClassName.replaceAll("\\.", "/") + ".java");
        try {
            return new ExpectedMatcher(fullyQualifiedMatcherClassName, readString(expectedMatcherSourceFile), record);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error resolving " + expectedMatcherSourceFile + ", " +
                    "because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }

    private static final RecordMatcherGenerator generator = new RecordMatcherGenerator();

    public String generatedSourceCode() {
        return generator.generateFromRecord(record());
    }

    public void assertEqualToGeneratedMatcherSourceCode() {
        var generatedSourceCode = generatedSourceCode();
        assertEquals(sourceCode(), generatedSourceCode);

        var compiledGeneratedMatcher = javac().compile(JavaFileObjects.forSourceString(fullyQualifiedClassName(), generatedSourceCode));
        assertAll(
                () -> assertThat(compiledGeneratedMatcher, where(Compilation::status, is(SUCCESS))),
                () -> assertThat(compiledGeneratedMatcher, where(Compilation::errors, empty())));
    }

}

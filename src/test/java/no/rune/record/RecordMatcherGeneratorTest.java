package no.rune.record;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import no.rune.record.matcher.EmptyRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.google.testing.compile.Compilation.Status.SUCCESS;
import static com.google.testing.compile.Compiler.javac;
import static no.rune.record.ExpectedMatcher.expectedMatcherFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;


class RecordMatcherGeneratorTest {

    private final RecordMatcherGenerator generator = new RecordMatcherGenerator();

    @Test
    void canOnlyGenerateFromRecord() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromRecord(String.class));
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromRecord(CharSequence.class));
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromRecord(InputStream.class));
    }


    @Test
    void generateMatcherForEmptyRecord() throws IOException {
        var expectedMatcher = expectedMatcherFor(EmptyRecord.class);

        var generatedSourceCode = generator.generateFromRecord(EmptyRecord.class);
        assertEquals(expectedMatcher.sourceCode(), generatedSourceCode);

        var compiledGeneratedMatcher = javac().compile(JavaFileObjects.forSourceString(expectedMatcher.fullyQualifiedClassName(), generatedSourceCode));
        assertAll(
                () -> assertThat(compiledGeneratedMatcher, where(Compilation::status, is(SUCCESS))),
                () -> assertThat(compiledGeneratedMatcher, where(Compilation::errors, empty())));
    }


}

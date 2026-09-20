package no.rune.typecompanion.generator.recordmatcher.example.nested;

import no.rune.typecompanion.generator.recordmatcher.RecordMatcherGenerator;
import org.junit.jupiter.api.Test;

import static no.rune.typecompanion.generator.JavaCompilationUnitMatcher.compiles;
import static no.rune.typecompanion.generator.JavaSourceFileReader.testSourcesReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NestedRecordsMatcherTest {

    @Test
    void generatesExpectedMatcherForOneLevelNestedRecord() {
        var generatedMatcher = new RecordMatcherGenerator().generateFor(TopLevel.Nested.class);
        assertThat(generatedMatcher, compiles());
        assertEquals(testSourcesReader.readSourceOf(TopLevelNestedMatcher.class), generatedMatcher.content());
    }

    @Test
    void generatesExpectedMatcherForTwoLevelsNestedRecord() {
        var generatedMatcher = new RecordMatcherGenerator().generateFor(TopLevel.Nested.EvenMore.class);
        assertEquals(testSourcesReader.readSourceOf(TopLevelNestedEvenMoreMatcher.class), generatedMatcher.content());
        assertThat(generatedMatcher, compiles());
    }

}

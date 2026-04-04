package no.rune.typecompanion.generator.recordmatcher.example.nested;

import org.junit.jupiter.api.Test;

import static no.rune.typecompanion.generator.recordmatcher.ExpectedMatcher.expectedMatcherFor;

class NestedRecordsMatcherTest {

    @Test
    void generatesExpectedMatcherForOneLevelNestedRecord() {
        expectedMatcherFor(TopLevel.Nested.class).assertEqualToGeneratedMatcherSourceCode();
    }

    @Test
    void generatesExpectedMatcherForTwoLevelsNestedRecord() {
        expectedMatcherFor(TopLevel.Nested.EvenMore.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

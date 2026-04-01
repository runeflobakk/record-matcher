package no.rune.record.matcher.example.nested;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;

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

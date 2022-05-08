package no.rune.record.matcher;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.EmptyRecordMatcher.anyEmptyRecord;
import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class EmptyRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, anyEmptyRecord()));
        assertThat(assertionError,
                where(AssertionError::getMessage, containsString("Expected: any EmptyRecord record\n     but: was null")));
    }

    @Test
    void matchesTheRecord() {
        assertThat(new EmptyRecord(), anyEmptyRecord());
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(EmptyRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

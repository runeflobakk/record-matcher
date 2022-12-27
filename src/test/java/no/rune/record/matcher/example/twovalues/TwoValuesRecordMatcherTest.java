package no.rune.record.matcher.example.twovalues;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static no.rune.record.matcher.example.twovalues.TwoValuesRecordMatcher.anyTwoValuesRecord;
import static no.rune.record.matcher.example.twovalues.TwoValuesRecordMatcher.twoValuesRecordWithNumber;
import static no.rune.record.matcher.example.twovalues.TwoValuesRecordMatcher.twoValuesRecordWithText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class TwoValuesRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, anyTwoValuesRecord()));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString("any " + TwoValuesRecord.class.getSimpleName() + " record\n     but: was null")));
    }

    @Test
    void matchesAnyRecord() {
        assertThat(new TwoValuesRecord("x", 0), anyTwoValuesRecord());
        assertThat(new TwoValuesRecord(null, 0), anyTwoValuesRecord());
    }

    @Test
    void matchesRecordWithText() {
        assertThat(new TwoValuesRecord("x", 0), twoValuesRecordWithText("x"));
        assertThat(new TwoValuesRecord(null, 0), twoValuesRecordWithText(nullValue()));
        assertThat(new TwoValuesRecord(null, 0), twoValuesRecordWithText((String) null));
    }

    @Test
    void matchesRecordWithNumber() {
        assertThat(new TwoValuesRecord("x", 1), twoValuesRecordWithNumber(1));
        assertThat(new TwoValuesRecord(null, 1), twoValuesRecordWithNumber(greaterThan(0)));
    }

    @Test
    void doesNotMatchRecordWithUnexpectedValue() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new TwoValuesRecord("x", 0), twoValuesRecordWithText("y")));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(TwoValuesRecord.class.getSimpleName() + " record where text is \"y\"\n     but: text was \"x\"")));
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(TwoValuesRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

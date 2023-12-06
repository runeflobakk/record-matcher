package no.rune.record.matcher.example.twovalues;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static no.rune.record.matcher.example.twovalues.TwoValuesRecordMatcher.aTwoValuesRecord;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class TwoValuesRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, aTwoValuesRecord()));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString("any " + TwoValuesRecord.class.getSimpleName() + " record\n     but: was null")));
    }

    @Test
    void matchesAnyRecord() {
        assertThat(new TwoValuesRecord("x", 0), aTwoValuesRecord());
        assertThat(new TwoValuesRecord(null, 0), aTwoValuesRecord());
    }

    @Test
    void matchesRecordWithText() {
        assertThat(new TwoValuesRecord("x", 0), aTwoValuesRecord().withText("x"));
        assertThat(new TwoValuesRecord(null, 0), aTwoValuesRecord().withText(nullValue()));
        assertThat(new TwoValuesRecord(null, 0), aTwoValuesRecord().withText((String) null));
    }

    @Test
    void matchesRecordWithNumber() {
        assertThat(new TwoValuesRecord("x", 1), aTwoValuesRecord().withNumber(1));
        assertThat(new TwoValuesRecord(null, 1), aTwoValuesRecord().withNumber(greaterThan(0)));
    }

    @Test
    void matchesRecordWithAllComponentsSpecified() {
        assertThat(new TwoValuesRecord("xyz", 1), aTwoValuesRecord().withNumber(1).withText(containsString("y")));
    }

    @Test
    void doesNotMatchRecordWithUnexpectedValue() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new TwoValuesRecord("x", 0), aTwoValuesRecord().withText("y")));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(TwoValuesRecord.class.getSimpleName() + " record where text is \"y\"\n     but:  text was \"x\"")));
    }

    @Test
    void doesNotMatchRecordWithTwoUnexpectedValues() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new TwoValuesRecord("x", 0), aTwoValuesRecord().withText(containsString("y")).withNumber(2)));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(TwoValuesRecord.class.getSimpleName() + " record where text a string containing \"y\" number is <2>\n     but:  text was \"x\" number was <0>")));
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(TwoValuesRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

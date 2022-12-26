package no.rune.record.matcher.example.singlestring;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static no.rune.record.matcher.example.singlestring.SingleStringRecordMatcher.anySingleStringRecord;
import static no.rune.record.matcher.example.singlestring.SingleStringRecordMatcher.singleStringRecordWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class SingleStringRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, anySingleStringRecord()));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString("any " + SingleStringRecord.class.getSimpleName() + " record\n     but: was null")));
    }

    @Test
    void matchesAnyRecord() {
        assertThat(new SingleStringRecord("x"), anySingleStringRecord());
        assertThat(new SingleStringRecord(null), anySingleStringRecord());
    }

    @Test
    void matchesRecordWithValue() {
        assertThat(new SingleStringRecord("x"), singleStringRecordWithValue("x"));
        assertThat(new SingleStringRecord(null), singleStringRecordWithValue(nullValue()));
        assertThat(new SingleStringRecord(null), singleStringRecordWithValue((String) null));
    }

    @Test
    void doesNotMatchRecordWithUnexpectedValue() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new SingleStringRecord("x"), singleStringRecordWithValue("y")));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(SingleStringRecord.class.getSimpleName() + " record where value is \"y\"\n     but: value was \"x\"")));
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(SingleStringRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

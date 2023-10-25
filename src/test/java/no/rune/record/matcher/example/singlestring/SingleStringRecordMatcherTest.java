package no.rune.record.matcher.example.singlestring;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static no.rune.record.matcher.example.singlestring.SingleStringRecordMatcher.aSingleStringRecord;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class SingleStringRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, aSingleStringRecord()));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString("any " + SingleStringRecord.class.getSimpleName() + " record\n     but: was null")));
    }

    @Test
    void matchesAnyRecord() {
        assertThat(new SingleStringRecord("x"), aSingleStringRecord());
        assertThat(new SingleStringRecord(null), aSingleStringRecord());
    }

    @Test
    void matchesRecordWithValue() {
        assertThat(new SingleStringRecord("x"), aSingleStringRecord().withValue("x"));
        assertThat(new SingleStringRecord(null), aSingleStringRecord().withValue(nullValue()));
        assertThat(new SingleStringRecord(null), aSingleStringRecord().withValue((String) null));
    }

    @Test
    void doesNotMatchRecordWithUnexpectedValue() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new SingleStringRecord("x"), aSingleStringRecord().withValue("y")));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(SingleStringRecord.class.getSimpleName() + " record where value is \"y\"\n     but: value was \"x\"")));
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(SingleStringRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

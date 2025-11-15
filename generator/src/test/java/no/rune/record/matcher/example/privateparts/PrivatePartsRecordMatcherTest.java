package no.rune.record.matcher.example.privateparts;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static no.rune.record.matcher.example.privateparts.PrivatePartsRecordMatcher.aPrivatePartsRecord;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class PrivatePartsRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, aPrivatePartsRecord()));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString("any " + PrivatePartsRecord.class.getSimpleName() + " record\n     but: was null")));
    }

    @Test
    void matchesAnyRecord() {
        assertThat(new PrivatePartsRecord(1, null), aPrivatePartsRecord());
        assertThat(new PrivatePartsRecord(Integer.MIN_VALUE, null), aPrivatePartsRecord());
    }

    @Test
    void matchesRecordWithValue() {
        assertThat(new PrivatePartsRecord(42, null), aPrivatePartsRecord().withNumber(42));
    }

    @Test
    void doesNotMatchRecordWithUnexpectedValue() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new PrivatePartsRecord(1, null), aPrivatePartsRecord().withNumber(42)));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(PrivatePartsRecord.class.getSimpleName() + " record where number is <42>\n     but:  number was <1>")));
    }

    @Test
    void doesNotMatchPrimitiveRecordComponentExpectedToBeNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new PrivatePartsRecord(1, null), aPrivatePartsRecord().withNumber(nullValue())));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(PrivatePartsRecord.class.getSimpleName() + " record where number null\n     but:  number was <1>")));
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(PrivatePartsRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

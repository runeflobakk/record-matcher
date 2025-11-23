package no.rune.record.matcher.example.privateparameterizedparts;

import org.junit.jupiter.api.Test;

import static no.rune.record.matcher.ExpectedMatcher.expectedMatcherFor;
import static no.rune.record.matcher.example.privateparameterizedparts.PrivatePartOfListRecordMatcher.aPrivatePartOfListRecord;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class PrivatePartOfListRecordMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, aPrivatePartOfListRecord()));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString("any " + PrivatePartOfListRecord.class.getSimpleName() + " record\n     but: was null")));
    }

    @Test
    void matchesAnyRecord() {
        assertThat(new PrivatePartOfListRecord(1, null), aPrivatePartOfListRecord());
        assertThat(new PrivatePartOfListRecord(Integer.MIN_VALUE, null), aPrivatePartOfListRecord());
    }

    @Test
    void matchesRecordWithValue() {
        assertThat(new PrivatePartOfListRecord(42, null), aPrivatePartOfListRecord().withNumber(42));
    }

    @Test
    void doesNotMatchRecordWithUnexpectedValue() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new PrivatePartOfListRecord(1, null), aPrivatePartOfListRecord().withNumber(42)));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(PrivatePartOfListRecord.class.getSimpleName() + " record where number is <42>\n     but:  number was <1>")));
    }

    @Test
    void doesNotMatchPrimitiveRecordComponentExpectedToBeNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(new PrivatePartOfListRecord(1, null), aPrivatePartOfListRecord().withNumber(nullValue())));
        assertThat(assertionError, where(AssertionError::getMessage,
                containsString(PrivatePartOfListRecord.class.getSimpleName() + " record where number null\n     but:  number was <1>")));
    }

    @Test
    void generatesExpectedMatcher() {
        expectedMatcherFor(PrivatePartOfListRecord.class).assertEqualToGeneratedMatcherSourceCode();
    }

}

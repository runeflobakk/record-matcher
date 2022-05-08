package no.rune.record.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class EmptyRecordMatcher extends TypeSafeDiagnosingMatcher<EmptyRecord> {
    private EmptyRecordMatcher() {
    }

    public static EmptyRecordMatcher anyEmptyRecord() {
        return new EmptyRecordMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("any " + EmptyRecord.class.getSimpleName() + " record");
    }

    @Override
    protected boolean matchesSafely(EmptyRecord element, Description mismatchDescription) {
        return true;
    }
}

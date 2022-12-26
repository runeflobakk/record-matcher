package no.rune.record.matcher.example.empty;

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
        description.appendText("any ").appendText(EmptyRecord.class.getSimpleName()).appendText(" record");
    }

    @Override
    protected boolean matchesSafely(EmptyRecord element, Description mismatchDescription) {
        boolean matches = true;
        return matches;
    }
}

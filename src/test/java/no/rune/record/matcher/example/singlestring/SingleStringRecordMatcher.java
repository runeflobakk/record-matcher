package no.rune.record.matcher.example.singlestring;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public final class SingleStringRecordMatcher extends TypeSafeDiagnosingMatcher<SingleStringRecord> {
    private final Matcher<? super String> valueMatcher;

    private SingleStringRecordMatcher(Matcher<? super String> valueMatcher) {
        this.valueMatcher = valueMatcher;
    }

    public static SingleStringRecordMatcher aSingleStringRecord() {
        return new SingleStringRecordMatcher(new IsAnything<>("any value"));
    }

    public SingleStringRecordMatcher withValue(String value) {
        return withValue(Matchers.is(value));
    }

    public SingleStringRecordMatcher withValue(Matcher<? super String> valueMatcher) {
        return new SingleStringRecordMatcher(valueMatcher);
    }

    @Override
    public void describeTo(Description description) {
        if (valueMatcher instanceof IsAnything) {
            description.appendText("any ").appendText(SingleStringRecord.class.getSimpleName()).appendText(" record");
        }
        else {
            description
                        .appendText(SingleStringRecord.class.getSimpleName()).appendText(" record where");
            if (!(valueMatcher instanceof IsAnything))
                description.appendText(" value ").appendDescriptionOf(valueMatcher);
        }
    }

    @Override
    protected boolean matchesSafely(SingleStringRecord element, Description mismatchDescription) {
        boolean matches = true;
        if (!valueMatcher.matches(element.value())) {
            mismatchDescription.appendText("value ");
            valueMatcher.describeMismatch(element.value(), mismatchDescription);
            matches = false;
        }
        return matches;
    }
}

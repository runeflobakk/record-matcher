package no.rune.record.matcher.example.privateparameterizedparts;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public final class PrivatePartOfListRecordMatcher extends TypeSafeDiagnosingMatcher<PrivatePartOfListRecord> {
    private final Matcher<? super Integer> numberMatcher;

    private PrivatePartOfListRecordMatcher(Matcher<? super Integer> numberMatcher) {
        this.numberMatcher = numberMatcher;
    }

    public static PrivatePartOfListRecordMatcher aPrivatePartOfListRecord() {
        return new PrivatePartOfListRecordMatcher(new IsAnything<>("any number"));
    }

    public PrivatePartOfListRecordMatcher withNumber(int number) {
        return withNumber(Matchers.is(number));
    }

    public PrivatePartOfListRecordMatcher withNumber(Matcher<? super Integer> numberMatcher) {
        return new PrivatePartOfListRecordMatcher(numberMatcher);
    }

    @Override
    public void describeTo(Description description) {
        if (numberMatcher instanceof IsAnything) {
            description.appendText("any ").appendText(PrivatePartOfListRecord.class.getSimpleName()).appendText(" record");
        }
        else {
            description
                        .appendText(PrivatePartOfListRecord.class.getSimpleName()).appendText(" record where");
            if (!(numberMatcher instanceof IsAnything))
                description.appendText(" number ").appendDescriptionOf(numberMatcher);
        }
    }

    @Override
    protected boolean matchesSafely(PrivatePartOfListRecord element,
            Description mismatchDescription) {
        boolean matches = true;
        if (!numberMatcher.matches(element.number())) {
            mismatchDescription.appendText(" number ");
            numberMatcher.describeMismatch(element.number(), mismatchDescription);
            matches = false;
        }
        return matches;
    }
}

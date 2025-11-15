package no.rune.record.matcher.example.privateparts;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public final class PrivatePartsRecordMatcher extends TypeSafeDiagnosingMatcher<PrivatePartsRecord> {
    private final Matcher<? super Integer> numberMatcher;

    private PrivatePartsRecordMatcher(Matcher<? super Integer> numberMatcher) {
        this.numberMatcher = numberMatcher;
    }

    public static PrivatePartsRecordMatcher aPrivatePartsRecord() {
        return new PrivatePartsRecordMatcher(new IsAnything<>("any number"));
    }

    public PrivatePartsRecordMatcher withNumber(int number) {
        return withNumber(Matchers.is(number));
    }

    public PrivatePartsRecordMatcher withNumber(Matcher<? super Integer> numberMatcher) {
        return new PrivatePartsRecordMatcher(numberMatcher);
    }

    @Override
    public void describeTo(Description description) {
        if (numberMatcher instanceof IsAnything) {
            description.appendText("any ").appendText(PrivatePartsRecord.class.getSimpleName()).appendText(" record");
        }
        else {
            description
                        .appendText(PrivatePartsRecord.class.getSimpleName()).appendText(" record where");
            if (!(numberMatcher instanceof IsAnything))
                description.appendText(" number ").appendDescriptionOf(numberMatcher);
        }
    }

    @Override
    protected boolean matchesSafely(PrivatePartsRecord element, Description mismatchDescription) {
        boolean matches = true;
        if (!numberMatcher.matches(element.number())) {
            mismatchDescription.appendText(" number ");
            numberMatcher.describeMismatch(element.number(), mismatchDescription);
            matches = false;
        }
        return matches;
    }
}

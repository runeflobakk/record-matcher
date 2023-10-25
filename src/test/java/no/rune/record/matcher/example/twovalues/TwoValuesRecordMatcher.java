package no.rune.record.matcher.example.twovalues;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public final class TwoValuesRecordMatcher extends TypeSafeDiagnosingMatcher<TwoValuesRecord> {
    private final Matcher<? super String> textMatcher;

    private final Matcher<? super Integer> numberMatcher;

    private TwoValuesRecordMatcher(Matcher<? super String> textMatcher,
            Matcher<? super Integer> numberMatcher) {
        this.textMatcher = textMatcher;
        this.numberMatcher = numberMatcher;
    }

    public static TwoValuesRecordMatcher aTwoValuesRecord() {
        return new TwoValuesRecordMatcher(new IsAnything<>("any text"), new IsAnything<>("any number"));
    }

    public TwoValuesRecordMatcher withText(String text) {
        return withText(Matchers.is(text));
    }

    public TwoValuesRecordMatcher withText(Matcher<? super String> textMatcher) {
        return new TwoValuesRecordMatcher(textMatcher, this.numberMatcher);
    }

    public TwoValuesRecordMatcher withNumber(int number) {
        return withNumber(Matchers.is(number));
    }

    public TwoValuesRecordMatcher withNumber(Matcher<? super Integer> numberMatcher) {
        return new TwoValuesRecordMatcher(this.textMatcher, numberMatcher);
    }

    @Override
    public void describeTo(Description description) {
        if (textMatcher instanceof IsAnything && numberMatcher instanceof IsAnything) {
            description.appendText("any ").appendText(TwoValuesRecord.class.getSimpleName()).appendText(" record");
        }
        else {
            description
                        .appendText(TwoValuesRecord.class.getSimpleName()).appendText(" record where");
            if (!(textMatcher instanceof IsAnything))
                description.appendText(" text ").appendDescriptionOf(textMatcher);

            if (!(numberMatcher instanceof IsAnything))
                description.appendText(" number ").appendDescriptionOf(numberMatcher);
        }
    }

    @Override
    protected boolean matchesSafely(TwoValuesRecord element, Description mismatchDescription) {
        boolean matches = true;
        if (!textMatcher.matches(element.text())) {
            mismatchDescription.appendText("text ");
            textMatcher.describeMismatch(element.text(), mismatchDescription);
            matches = false;
        }
        if (!numberMatcher.matches(element.number())) {
            mismatchDescription.appendText("number ");
            numberMatcher.describeMismatch(element.number(), mismatchDescription);
            matches = false;
        }
        return matches;
    }
}

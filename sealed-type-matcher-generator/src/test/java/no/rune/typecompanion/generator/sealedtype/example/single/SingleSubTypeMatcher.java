package no.rune.typecompanion.generator.sealedtype.example.single;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class SingleSubTypeMatcher<CASE extends SingleSubType> extends TypeSafeDiagnosingMatcher<SingleSubType> {
    private final Class<CASE> expectedCase;

    private final Matcher<? super CASE> matcher;

    private SingleSubTypeMatcher(Class<CASE> expectedCase, Matcher<? super CASE> matcher) {
        this.expectedCase = expectedCase;
        this.matcher = matcher;
    }

    public static SingleSubTypeMatcher<SingleSubType.SingleImpl> caseOfSingleImpl() {
        return caseOfSingleImpl(null);
    }

    public static SingleSubTypeMatcher<SingleSubType.SingleImpl> caseOfSingleImpl(
            Matcher<? super SingleSubType.SingleImpl> matcher) {
        return new SingleSubTypeMatcher<>(SingleSubType.SingleImpl.class, matcher);
    }

    @Override
    public void describeTo(Description description) {
        if (matcher == null) {
            description.appendText("any case of ").appendText(expectedCase.getSimpleName());
        } else {
            description.appendText("case of ").appendText(expectedCase.getSimpleName())
                .appendText(", matching ").appendDescriptionOf(matcher);
        }
    }

    @Override
    protected boolean matchesSafely(SingleSubType item, Description mismatchDescription) {
        boolean matches = true;
        if (!expectedCase.isInstance(item)) {
            mismatchDescription.appendText("a " + item.getClass().getSimpleName());
            matches = false;
        } else if (matcher != null && !matcher.matches(item)) {
            mismatchDescription.appendText("the ").appendText(item.getClass().getSimpleName()).appendText(" ");
            matcher.describeMismatch(item, mismatchDescription);
            matches = false;
        }
        return matches;
    }
}

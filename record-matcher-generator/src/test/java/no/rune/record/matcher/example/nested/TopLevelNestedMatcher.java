package no.rune.record.matcher.example.nested;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public final class TopLevelNestedMatcher extends TypeSafeDiagnosingMatcher<TopLevel.Nested> {
    private final Matcher<? super Integer> valueMatcher;

    private TopLevelNestedMatcher(Matcher<? super Integer> valueMatcher) {
        this.valueMatcher = valueMatcher;
    }

    public static TopLevelNestedMatcher aNested() {
        return new TopLevelNestedMatcher(new IsAnything<>("any value"));
    }

    public TopLevelNestedMatcher withValue(int value) {
        return withValue(Matchers.is(value));
    }

    public TopLevelNestedMatcher withValue(Matcher<? super Integer> valueMatcher) {
        return new TopLevelNestedMatcher(valueMatcher);
    }

    @Override
    public void describeTo(Description description) {
        if (valueMatcher instanceof IsAnything) {
            description.appendText("any ").appendText(TopLevel.Nested.class.getSimpleName()).appendText(" record");
        }
        else {
            description
                        .appendText(TopLevel.Nested.class.getSimpleName()).appendText(" record where");
            if (!(valueMatcher instanceof IsAnything))
                description.appendText(" value ").appendDescriptionOf(valueMatcher);
        }
    }

    @Override
    protected boolean matchesSafely(TopLevel.Nested element, Description mismatchDescription) {
        boolean matches = true;
        if (!valueMatcher.matches(element.value())) {
            mismatchDescription.appendText(" value ");
            valueMatcher.describeMismatch(element.value(), mismatchDescription);
            matches = false;
        }
        return matches;
    }
}

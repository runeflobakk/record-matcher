package no.rune.record.matcher.example.nested;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class TopLevelNestedEvenMoreMatcher extends TypeSafeDiagnosingMatcher<TopLevel.Nested.EvenMore> {
    private TopLevelNestedEvenMoreMatcher() {
    }

    public static TopLevelNestedEvenMoreMatcher anEvenMore() {
        return new TopLevelNestedEvenMoreMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("any ").appendText(TopLevel.Nested.EvenMore.class.getSimpleName()).appendText(" record");
    }

    @Override
    protected boolean matchesSafely(TopLevel.Nested.EvenMore element,
            Description mismatchDescription) {
        boolean matches = true;
        return matches;
    }
}

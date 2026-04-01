package no.rune.text;

import no.rune.text.Casing.Style;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CasingTest {

    @Test
    void convertsToPascalCase() {
        assertThat(Casing.to(Style.PascalCase, "a", "late", "night"), is("ALateNight"));
        assertThat(Casing.to(Style.PascalCase, "an", "early", "morning"), is("AnEarlyMorning"));
    }

    @Test
    void convertsToCamelCase() {
        assertThat(Casing.to(Style.camelCase, "a", "late", "night"), is("aLateNight"));
        assertThat(Casing.to(Style.camelCase, "an", "early", "morning"), is("anEarlyMorning"));
    }

}

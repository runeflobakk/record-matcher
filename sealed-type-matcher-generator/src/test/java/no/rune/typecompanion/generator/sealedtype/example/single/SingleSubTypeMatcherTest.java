package no.rune.typecompanion.generator.sealedtype.example.single;

import no.rune.typecompanion.generator.sealedtype.SealedTypeMatcherGenerator;
import no.rune.typecompanion.generator.sealedtype.example.single.SingleSubType.SingleImpl;
import org.junit.jupiter.api.Test;

import static no.rune.typecompanion.generator.JavaCompilationUnitMatcher.compiles;
import static no.rune.typecompanion.generator.JavaSourceFileReader.testSourcesReader;
import static no.rune.typecompanion.generator.sealedtype.example.single.SingleSubTypeMatcher.caseOfSingleImpl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class SingleSubTypeMatcherTest {

    @Test
    void doesNotMatchNull() {
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(null, caseOfSingleImpl()));
        assertThat(assertionError, where(AssertionError::getMessage, matchesPattern("""
            (?s).*any case of SingleImpl.*
            .+but: was null\
            """)));
    }

    @Test
    void matchesAnyInstanceOfThePermittedSubType() {
        SingleSubType singleSubType = new SingleImpl("x");
        assertThat(singleSubType, caseOfSingleImpl());
    }

    @Test
    void expectedSubTypeButFailsMatch() {
        SingleSubType singleSubType = new SingleImpl("x");
        var assertionError = assertThrows(AssertionError.class, () -> assertThat(singleSubType, caseOfSingleImpl(where(SingleImpl::text, is("y")))));
        assertThat(assertionError, where(AssertionError::getMessage, matchesPattern("""
            (?s).*case of SingleImpl.+with a text.+which is "y".*
            .*but: the SingleImpl had the text.+"x"\
            """)));
    }

    @Test
    void matchesOnExpectedSubType() {
        SingleSubType singleSubType = new SingleImpl("y");
        assertThat(singleSubType, caseOfSingleImpl(where(SingleImpl::text, is("y"))));
    }


    @Test
    void generatesExpectedMatcher() {
        var generatedMatcher = new SealedTypeMatcherGenerator().generateFor(SingleSubType.class);
        assertEquals(testSourcesReader.readSourceOf(SingleSubTypeMatcher.class), generatedMatcher.content());
        assertThat(generatedMatcher, compiles());
    }
}

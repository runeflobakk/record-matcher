package no.rune.record.matcher;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class DefaultRecordMatcherClassNameResolverTest {

    DefaultRecordMatcherClassNameResolver nameResolver = new DefaultRecordMatcherClassNameResolver();

    @Test
    void topLevelRecordResolvesMatcherClassName() {
        assertThat(Foo.class, where(nameResolver::resolve, is("FooMatcher")));
    }

    @Test
    void firstLevelNestedMatcherClassName() {
        assertThat(Foo.Bar.class, where(nameResolver::resolve, is("FooBarMatcher")));
    }

    @Test
    void secondLevelNestedMatcherClassName() {
        assertThat(Foo.Bar.Baz.class, where(nameResolver::resolve, is("FooBarBazMatcher")));
    }
}


record Foo() {
    record Bar() {
        record Baz() {}
    }
}

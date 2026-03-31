package no.rune.record.matcher;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.groupingBy;
import static no.rune.record.matcher.Collectors.multiGroupingBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.lists;

class CollectorsTest {

    @Nested
    class MultiGrouping {
        @Test
        void intoExclusiveGroupsIsTheSameAsJdkStreamCollectorGroupingBy() {
            enum Classification {
                ODD, EVEN;
                static Classification classify(int i) {
                    return i % 2 == 0 ? EVEN : ODD;
                }
            }
            qt()
                .forAll(lists().of(integers().all()).ofSizeBetween(0, 1_000))
                .checkAssert(ints -> assertThat(
                        ints.stream().parallel().collect(multiGroupingBy(i -> List.of(Classification.classify(i)))),
                        equalTo(ints.stream().parallel().collect(groupingBy(Classification::classify)))));
        }

        @Test
        void assignsElementsToMultipleMapKeys() {
            enum Classification {
                ODD, EVEN, POSITIVE, NEGATIVE, ZERO;
                static List<Classification> classify(int i) {
                    return List.of(i % 2 == 0 ? EVEN : ODD, i == 0 ? ZERO : (abs(i) == i ? POSITIVE : NEGATIVE));
                }
            }

            var classifiedInts = Stream.of(-7, -5, -4, 0, 10, 11, 17, 22).collect(multiGroupingBy(Classification::classify));
            assertAll(
                    () -> assertThat(classifiedInts.get(Classification.ODD), containsInAnyOrder(-7, -5, 11, 17)),
                    () -> assertThat(classifiedInts.get(Classification.EVEN), containsInAnyOrder(-4, 0, 10, 22)),
                    () -> assertThat(classifiedInts.get(Classification.ZERO), containsInAnyOrder(0)),
                    () -> assertThat(classifiedInts.get(Classification.POSITIVE), containsInAnyOrder(10, 11, 17, 22)),
                    () -> assertThat(classifiedInts.get(Classification.NEGATIVE), containsInAnyOrder(-7, -5, -4))
                    );
        }

        @Test
        void unclassifiedElementsAreDiscarded() {
            enum Signum {
                POSITIVE, NEGATIVE;

                public boolean appliesTo(int i) {
                    return i != 0 && (abs(i) == i ? this == POSITIVE : this == NEGATIVE);
                }
            }

            var intsBySignum = Stream.of(-2, -1, 0, 1, 2).collect(multiGroupingBy(List.of(Signum.values()), Signum::appliesTo));
            assertAll(
                    () -> assertThat(intsBySignum.get(Signum.POSITIVE), containsInAnyOrder(1, 2)),
                    () -> assertThat(intsBySignum.get(Signum.NEGATIVE), containsInAnyOrder(-1, -2)),
                    () -> assertThat(intsBySignum, not(hasEntry(anything(), hasItem(0))))
                    );
        }
    }
}

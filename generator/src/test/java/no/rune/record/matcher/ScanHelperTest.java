package no.rune.record.matcher;

import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.stream.Stream.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static uk.co.probablyfine.matchers.Java8Matchers.whereNot;

class ScanHelperTest {

    @Nested
    class AccessibleFromSamePackage {
        record Basic(String s, List<Long> nums) {}
        @Test
        void basicRecord() {
            assertAll(concat(Stream.of(Basic.class), Stream.of(Basic.class.getRecordComponents()).map(RecordComponent::getGenericType))
                    .map(type -> () -> assertThat(type.getTypeName(), type, where(ScanHelper::isAccessibleFromSamePackage))));
        }
    }

    @Nested
    class NotAccessibleRecord {
        @Test
        void localRecord() {
            record Local() {}
            assertThat(Local.class, whereNot(ScanHelper::isAccessibleFromSamePackage));
        }

        private record Private() {}
        @Test
        void privateRecord() {
            assertThat(Private.class, whereNot(ScanHelper::isAccessibleFromSamePackage));
        }
    }

    @Nested
    class GenericArray {
        record HorribleButAccessible(List<String>[] listsOfStrings) {}

        @Test
        void accessibleGenericArray() {
            assertAll(Stream.of(HorribleButAccessible.class.getRecordComponents()).map(RecordComponent::getGenericType)
                    .map(genericArray -> () -> assertThat(genericArray.getTypeName(), genericArray, where(ScanHelper::isAccessibleFromSamePackage))));
        }

        record HorribleJustDont(X[] whoKnows, Set<X>[] setsOfWhoKnows) {
            private class X {}
        }

        @Test
        void notAccessibleGenericArrays() {
            assertAll(Stream.of(HorribleJustDont.class.getRecordComponents()).map(RecordComponent::getGenericType)
                    .map(genericArray -> () -> assertThat(genericArray.getTypeName(), genericArray, whereNot(ScanHelper::isAccessibleFromSamePackage))));
        }
    }

    @Nested
    class TypeVariables {
        record Generic<T>(T t) {}

        @Test
        void typeVariablesAreNotAccessible() {
            assertAll(Stream.of(Generic.class.getRecordComponents()).map(RecordComponent::getGenericType)
                    .map(genericArray -> () -> assertThat(genericArray.getTypeName(), genericArray, whereNot(ScanHelper::isAccessibleFromSamePackage))));
        }
    }
}

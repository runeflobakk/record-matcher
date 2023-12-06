package no.rune.record;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.squareup.javapoet.WildcardTypeName.supertypeOf;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class CodeFactory {

    static final class RecordComponentCodeFactory {
        final RecordComponent recordComponent;

        RecordComponentCodeFactory(RecordComponent recordComponent) {
            this.recordComponent = recordComponent;
        }

        String componentName() {
            return recordComponent.getName();
        }

        Type componentType() {
            return recordComponent.getGenericType();
        }

        boolean isFor(RecordComponent component) {
            return recordComponent.equals(component);
        }

        TypeName matcherValueType() {
            TypeName typeName = TypeName.get(recordComponent.getGenericType());
            return typeName.isPrimitive() ? typeName.box() : typeName;
        }

        ParameterizedTypeName matcherType() {
            return ParameterizedTypeName.get(HAMCREST_MATCHER_CLASSNAME, supertypeOf(matcherValueType()));
        }

        String matcherFieldName() {
            return recordComponent.getName() + "Matcher";
        }

        FieldSpec newMatcherField() {
            return FieldSpec.builder(matcherType(), matcherFieldName())
                    .addModifiers(PRIVATE, FINAL)
                    .build();
        }

    }

    private static final ClassName HAMCREST_MATCHER_CLASSNAME = ClassName.get(Matcher.class);

    final Class<? extends Record> record;
    final ClassName matcherClass;

    private final Map<RecordComponent, CodeBlock> defaultComponentMatchers;

    CodeFactory(Class<? extends Record> record, ClassName matcherClass) {
        this.defaultComponentMatchers = Stream.of(record.getRecordComponents()).collect(collectingAndThen(toMap(
                identity(), CodeFactory::isAnythingMatcher,
                (v1, v2) -> { throw new IllegalStateException("Got same index for " + v1 + " and " + v2); }, LinkedHashMap::new), Collections::unmodifiableMap));
        this.record = record;
        this.matcherClass = matcherClass;
    }

    static CodeBlock isAnythingMatcher(RecordComponent component) {
        return CodeBlock.of("new $T<>(\"any $L\")", IsAnything.class, component.getName());
    }

    TypeSpec.Builder newMatcherClass() {
        return TypeSpec.classBuilder(matcherClass)
                .addModifiers(PUBLIC, FINAL)
                .superclass(ParameterizedTypeName.get(TypeSafeDiagnosingMatcher.class, record));
    }

    MethodSpec.Builder newBuilderLikeMethod(String methodName) {
        return MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC).returns(matcherClass);
    }

    MethodSpec.Builder newStaticFactoryMethod(String methodName) {
        return MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC).returns(matcherClass);
    }

    boolean isEmptyRecord() {
        return defaultComponentMatchers.isEmpty();
    }

    Stream<RecordComponentCodeFactory> components() {
        return defaultComponentMatchers.keySet().stream().map(RecordComponentCodeFactory::new);
    }

    Stream<CodeBlock> constructorArgs(Map<RecordComponent, CodeBlock> componentMatchers) {
        return defaultComponentMatchers.entrySet().stream().map(e -> componentMatchers.getOrDefault(e.getKey(), defaultComponentMatchers.get(e.getKey())));
    }

    CodeBlock defaultConstructorInvocation() {
        return constructorInvocation(emptyMap());
    }

    CodeBlock constructorInvocation(Map<RecordComponent, CodeBlock> componentMatchers) {
        return CodeBlock.of("new $T($L)", matcherClass, constructorArgs(componentMatchers).collect(CodeBlock.joining(", ")));
    }

    CodeBlock incrementalConstructorInvocation(RecordComponent component, CodeBlock componentMatcher) {
        return constructorInvocation(components().collect(toMap(c -> c.recordComponent, c -> c.isFor(component) ? componentMatcher : CodeBlock.of("this." + c.matcherFieldName()))));
    }

}
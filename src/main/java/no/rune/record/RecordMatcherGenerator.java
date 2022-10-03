package no.rune.record;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

import java.lang.reflect.RecordComponent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.squareup.javapoet.CodeBlock.joining;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.WildcardTypeName.supertypeOf;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static no.rune.text.Casing.mapCharAt;

public class RecordMatcherGenerator {

    private static final class CodeFactory {
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

        MethodSpec.Builder newStaticFactoryMethod(String methodName) {
            return MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC).returns(matcherClass);
        }

        boolean isEmptyRecord() {
            return defaultComponentMatchers.isEmpty();
        }

        Set<RecordComponent> components() {
            return defaultComponentMatchers.keySet();
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

    }


    public String generateFromRecord(Class<? extends Record> record) {
        return generateFromRecord(record, record.getPackage(), record.getSimpleName() + "Matcher");
    }

    public String generateFromRecord(Class<? extends Record> record, Package target, String matcherSimpleClassName) {

        var codeFactory = new CodeFactory(record, ClassName.get(target.getName(), matcherSimpleClassName));

        var anyRecordFactoryMethodBuilder = codeFactory
                .newStaticFactoryMethod("any" + record.getSimpleName())
                .addStatement("return $L", codeFactory.defaultConstructorInvocation());

        var matcherClassBuilder = codeFactory.newMatcherClass()
                .addMethod(anyRecordFactoryMethodBuilder.build());

        var privateConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(PRIVATE);

        codeFactory.components().forEach(component -> {
            FieldSpec matcherField = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Matcher.class), supertypeOf(component.getGenericType())), component.getName() + "Matcher")
                .addModifiers(PRIVATE, FINAL)
                .build();
            matcherClassBuilder.addField(matcherField);
            ParameterSpec constructorMatcherParam = ParameterSpec.builder(matcherField.type, matcherField.name).build();
            privateConstructorBuilder
                .addParameter(constructorMatcherParam)
                .addStatement("this.$N = $N", matcherField.name, constructorMatcherParam.name);

            MethodSpec withComponentMatchingMethod = codeFactory
                    .newStaticFactoryMethod(mapCharAt(0, record.getSimpleName(), Character::toLowerCase) + "With" + mapCharAt(0, component.getName(), Character::toUpperCase))
                    .addParameter(constructorMatcherParam)
                    .addStatement("return $L", codeFactory.constructorInvocation(Map.of(component, CodeBlock.of(constructorMatcherParam.name))))
                    .build();
            MethodSpec withComponentEqualToMethod = codeFactory
                    .newStaticFactoryMethod(mapCharAt(0, record.getSimpleName(), Character::toLowerCase) + "With" + mapCharAt(0, component.getName(), Character::toUpperCase))
                    .addParameter(component.getGenericType(), component.getName())
                    .addCode("return " + withComponentMatchingMethod.name + "($T.is(" + component.getName() + "));", Matchers.class)
                    .build();
            matcherClassBuilder
                .addMethod(withComponentEqualToMethod)
                .addMethod(withComponentMatchingMethod);
        });

        matcherClassBuilder
            .addMethod(privateConstructorBuilder.build());


        var anyRecordDescription = CodeBlock.builder().addStatement("description.appendText(\"any \").appendText($T.class.getSimpleName()).appendText(\" record\")", record).build();
        if (!codeFactory.isEmptyRecord()) {
            var allMatchesAnything = codeFactory.components().stream().map(c -> c.getName() + "Matcher instanceof $T")
                    .map(matcherTypeConstraing -> CodeBlock.of(matcherTypeConstraing, IsAnything.class))
                    .collect(joining(" && "));
            anyRecordDescription = CodeBlock.builder()
                    .beginControlFlow("if ($L)", allMatchesAnything)
                    .add(anyRecordDescription)
                    .endControlFlow()
                    .build();
        }

        var componentMatchingDescriptions = codeFactory.components().stream()
                .map(c -> CodeBlock.builder()
                        .addNamed(
                            """
                            if (!($componentMatcher:N instanceof $isAnythingMatcherType:T))
                                description.appendText(" $componentName:N ").appendDescriptionOf($componentMatcher:N);
                            """,
                            Map.of("componentName", c.getName(), "componentMatcher", c.getName() + "Matcher", "isAnythingMatcherType", IsAnything.class))
                        .build());


        var describeToMethodBuilder = MethodSpec.methodBuilder("describeTo")
                .addModifiers(PUBLIC)
                .addParameter(Description.class, "description")
                .addAnnotation(Override.class)
                .addCode(anyRecordDescription);

        if (!codeFactory.isEmptyRecord()) {
            var constrainedRecordDescription = CodeBlock.builder()
                    .beginControlFlow("else")
                    .addStatement(CodeBlock.of("""
                        description
                            .appendText($T.class.getSimpleName()).appendText(" record where")""",
                            record))
                    .add(componentMatchingDescriptions.collect(joining("\n")))
                    .endControlFlow()
                    .build();
            describeToMethodBuilder.addCode(constrainedRecordDescription);
        }
        var describeToMethod = describeToMethodBuilder.build();

        MethodSpec.Builder matchesSafelyMethodBuilder = MethodSpec.methodBuilder("matchesSafely")
                .addModifiers(PROTECTED)
                .addParameter(record, "element")
                .addParameter(Description.class, "mismatchDescription")
                .addAnnotation(Override.class)
                .returns(BOOLEAN);

        matchesSafelyMethodBuilder.addStatement("boolean matches = true");
        codeFactory
            .components().stream()
            .map(component -> CodeBlock.builder().addNamed(
                    """
                    if (!$matcherReference:N.matches(element.$componentName:N())) {
                        mismatchDescription.appendText("$componentName:N ");
                        $matcherReference:N.describeMismatch(element.$componentName:N(), mismatchDescription);
                        matches = false;
                    }
                    """,
                    Map.of("componentName", component.getName(), "matcherReference", component.getName() + "Matcher")))
            .map(CodeBlock.Builder::build)
            .forEach(matchesSafelyMethodBuilder::addCode);
        matchesSafelyMethodBuilder.addStatement("return matches");

        matcherClassBuilder
            .addMethod(describeToMethod)
            .addMethod(matchesSafelyMethodBuilder.build());


        return JavaFile.builder(target.getName(), matcherClassBuilder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build().toString();
    }




}


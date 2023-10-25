package no.rune.record;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsAnything;

import java.util.Map;

import static com.squareup.javapoet.CodeBlock.joining;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static no.rune.text.Affixing.withIndefArticle;
import static no.rune.text.Casing.mapCharAt;
import static no.rune.text.Casing.Style.camelCase;

public class RecordMatcherGenerator {

    public String generateFromRecord(Class<? extends Record> record) {
        return generateFromRecord(record, record.getPackage(), record.getSimpleName() + "Matcher");
    }

    public String generateFromRecord(Class<? extends Record> record, Package target, String matcherSimpleClassName) {

        var codeFactory = new CodeFactory(record, ClassName.get(target.getName(), matcherSimpleClassName));

        var anyRecordFactoryMethodBuilder = codeFactory
                .newStaticFactoryMethod(withIndefArticle(record.getSimpleName(), camelCase))
                .addStatement("return $L", codeFactory.defaultConstructorInvocation());

        var matcherClassBuilder = codeFactory.newMatcherClass()
                .addMethod(anyRecordFactoryMethodBuilder.build());

        var privateConstructorBuilder = MethodSpec.constructorBuilder().addModifiers(PRIVATE);

        codeFactory.components().forEach(component -> {
            FieldSpec matcherField = component.newMatcherField();
            matcherClassBuilder.addField(matcherField);
            ParameterSpec constructorMatcherParam = ParameterSpec.builder(matcherField.type, matcherField.name).build();
            privateConstructorBuilder
                .addParameter(constructorMatcherParam)
                .addStatement("this.$N = $N", matcherField.name, constructorMatcherParam.name);

            MethodSpec withComponentMatchingMethod = codeFactory
                    .newBuilderLikeMethod("with" + mapCharAt(0, component.componentName(), Character::toUpperCase))
                    .addParameter(constructorMatcherParam)
                    .addStatement("return $L", codeFactory.incrementalConstructorInvocation(component.recordComponent, CodeBlock.of(constructorMatcherParam.name)))
                    .build();
            MethodSpec withComponentEqualToMethod = codeFactory
                    .newBuilderLikeMethod("with" + mapCharAt(0, component.componentName(), Character::toUpperCase))
                    .addParameter(component.componentType(), component.componentName())
                    .addCode("return " + withComponentMatchingMethod.name + "($T.is(" + component.componentName() + "));", Matchers.class)
                    .build();
            matcherClassBuilder
                .addMethod(withComponentEqualToMethod)
                .addMethod(withComponentMatchingMethod);
        });

        matcherClassBuilder
            .addMethod(privateConstructorBuilder.build());


        var anyRecordDescription = CodeBlock.builder().addStatement("description.appendText(\"any \").appendText($T.class.getSimpleName()).appendText(\" record\")", record).build();
        if (!codeFactory.isEmptyRecord()) {
            var allMatchesAnything = codeFactory.components().map(c -> c.componentName() + "Matcher instanceof $T")
                    .map(matcherTypeConstraing -> CodeBlock.of(matcherTypeConstraing, IsAnything.class))
                    .collect(joining(" && "));
            anyRecordDescription = CodeBlock.builder()
                    .beginControlFlow("if ($L)", allMatchesAnything)
                    .add(anyRecordDescription)
                    .endControlFlow()
                    .build();
        }

        var componentMatchingDescriptions = codeFactory.components()
                .map(c -> CodeBlock.builder()
                        .addNamed(
                            """
                            if (!($componentMatcher:N instanceof $isAnythingMatcherType:T))
                                description.appendText(" $componentName:N ").appendDescriptionOf($componentMatcher:N);
                            """,
                            Map.of("componentName", c.componentName(), "componentMatcher", c.componentName() + "Matcher", "isAnythingMatcherType", IsAnything.class))
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
            .components()
            .map(component -> CodeBlock.builder().addNamed(
                    """
                    if (!$matcherReference:N.matches(element.$componentName:N())) {
                        mismatchDescription.appendText("$componentName:N ");
                        $matcherReference:N.describeMismatch(element.$componentName:N(), mismatchDescription);
                        matches = false;
                    }
                    """,
                    Map.of("componentName", component.componentName(), "matcherReference", component.componentName() + "Matcher")))
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


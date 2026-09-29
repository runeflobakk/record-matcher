package no.rune.typecompanion.generator.sealedtype;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import no.rune.typecompanion.generator.JavaCompilationUnit;
import no.rune.typecompanion.generator.TypeCompanionGenerator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Map;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static com.squareup.javapoet.WildcardTypeName.supertypeOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static no.rune.typecompanion.generator.sealedtype.ScanHelper.isAccessibleFromSamePackage;

public class SealedTypeMatcherGenerator implements TypeCompanionGenerator {

    public static final TypeCompanionClassNameResolver DEFAULT_MATCHER_NAME_RESOLVER = new DefaultMatcherClassNameResolver();

    @Override
    public JavaCompilationUnit generateFor(Class<?> type) {
        return generateFromSealedType(type, type.getPackage(), DEFAULT_MATCHER_NAME_RESOLVER.resolve(type));
    }

    JavaCompilationUnit generateFromSealedType(Class<?> sealedType, Package target, String matcherSimpleClassName) {

        var matcherClassName = ClassName.get(target.getName(), matcherSimpleClassName);
        var expectedCaseTypeVariable = TypeVariableName.get("CASE", sealedType);
        var expectedCaseTypeName = ParameterizedTypeName.get(ClassName.get(Class.class), expectedCaseTypeVariable);
        var caseMatcherTypeName = ParameterizedTypeName.get(ClassName.get(Matcher.class), supertypeOf(expectedCaseTypeVariable));

        var expectedCaseField = FieldSpec.builder(expectedCaseTypeName, "expectedCase", PRIVATE, FINAL).build();
        var caseMatcherField = FieldSpec.builder(caseMatcherTypeName, "matcher", PRIVATE, FINAL).build();

        var matcherClassBuilder = classBuilder(matcherClassName)
                .addModifiers(PUBLIC, FINAL)
                .addTypeVariable(expectedCaseTypeVariable)
                .superclass(ParameterizedTypeName.get(TypeSafeDiagnosingMatcher.class, sealedType))

                .addField(expectedCaseField)
                .addField(caseMatcherField)

                .addMethod(constructorBuilder().addModifiers(PRIVATE)
                        .addParameter(expectedCaseTypeName, expectedCaseField.name)
                        .addParameter(caseMatcherTypeName, caseMatcherField.name)
                        .addStatement("this.$L = $L", expectedCaseField.name, expectedCaseField.name)
                        .addStatement("this.$L = $L", caseMatcherField.name, caseMatcherField.name)
                        .build());

        for (Class<?> subType : sealedType.getPermittedSubclasses()) {
            if (!isAccessibleFromSamePackage(subType)) {
                continue;
            }
            var factoryMethodName = "caseOf" + subType.getSimpleName();
            matcherClassBuilder.addMethod(methodBuilder(factoryMethodName)
                    .addModifiers(PUBLIC, STATIC)
                    .returns(ParameterizedTypeName.get(matcherClassName, ClassName.get(subType)))
                    .addStatement("return $L(null)", factoryMethodName)
                    .build());

            matcherClassBuilder.addMethod(methodBuilder(factoryMethodName)
                    .addModifiers(PUBLIC, STATIC)
                    .addParameter(ParameterizedTypeName.get(ClassName.get(Matcher.class), supertypeOf(subType)), caseMatcherField.name)
                    .returns(ParameterizedTypeName.get(matcherClassName, ClassName.get(subType)))
                    .addStatement("return new $T<>($T.class, $L)", matcherClassName, subType, caseMatcherField.name)
                    .build());
        }


        var describeToMethodBuilder = MethodSpec.methodBuilder("describeTo")
                .addModifiers(PUBLIC)
                .addParameter(Description.class, "description")
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder()
                        .addNamed("""
                            if ($matcherField:L == null) {
                                description.appendText("any case of ").appendText($expectedCaseField:L.getSimpleName());
                            } else {
                                description.appendText("case of ").appendText($expectedCaseField:L.getSimpleName())
                                    .appendText(", matching ").appendDescriptionOf($matcherField:L);
                            }
                            """, Map.of(
                                    "matcherField", caseMatcherField.name,
                                    "expectedCaseField", expectedCaseField.name))
                        .build());

        matcherClassBuilder.addMethod(describeToMethodBuilder.build());


        MethodSpec.Builder matchesSafelyMethodBuilder = MethodSpec.methodBuilder("matchesSafely")
                .addModifiers(PROTECTED)
                .addParameter(sealedType, "item")
                .addParameter(Description.class, "mismatchDescription")
                .addAnnotation(Override.class)
                .returns(BOOLEAN)
                .addStatement("boolean matches = true")
                .addCode(CodeBlock.builder()
                        .addNamed("""
                            if (!$expectedCaseField:L.isInstance(item)) {
                                mismatchDescription.appendText("a " + item.getClass().getSimpleName());
                                matches = false;
                            } else if ($matcherField:L != null && !$matcherField:L.matches(item)) {
                                mismatchDescription.appendText("the ").appendText(item.getClass().getSimpleName()).appendText(" ");
                                $matcherField:L.describeMismatch(item, mismatchDescription);
                                matches = false;
                            }
                            """, Map.of(
                                    "expectedCaseField", expectedCaseField.name,
                                    "matcherField", caseMatcherField.name))
                        .build())
                .addStatement("return matches");

        matcherClassBuilder.addMethod(matchesSafelyMethodBuilder.build());


        return new JavaCompilationUnit(
                JavaFile.builder(target.getName(), matcherClassBuilder.build())
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build().toString(),
                matcherSimpleClassName,
                target);
    }

}

package no.rune.record;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class RecordMatcherGenerator {

    public String generateFromRecord(Class<? extends Record> record) {
        return generateFromRecord(record, record.getPackage(), record.getSimpleName() + "Matcher");
    }

    public String generateFromRecord(Class<? extends Record> record, Package target, String matcherSimpleClassName) {

        var fullyQualifiedMatcherClassName = ClassName.get(target.getName(), matcherClassName);

        var anyRecordFactoryMethod = MethodSpec.methodBuilder("any" + record.getSimpleName())
                .addModifiers(PUBLIC, STATIC)
                .returns(fullyQualifiedMatcherClassName)
                .addCode("return new $T();", fullyQualifiedMatcherClassName)
                .build();

        var describeToMethod = MethodSpec.methodBuilder("describeTo")
                .addModifiers(PUBLIC)
                .addParameter(Description.class, "description")
                .addAnnotation(Override.class)
                .addCode("description.appendText(\"any \" + " + record.getSimpleName() + ".class.getSimpleName() + \" record\");")
                .build();

        var matchesSafelyMethod = MethodSpec.methodBuilder("matchesSafely")
                .addModifiers(PROTECTED)
                .addParameter(record, "element")
                .addParameter(Description.class, "mismatchDescription")
                .addAnnotation(Override.class)
                .returns(BOOLEAN)
                .addCode("return true;")
                .build();

        var privateConstructor = MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE)
                .build();

        var matcherClass = TypeSpec.classBuilder(fullyQualifiedMatcherClassName)
                .addModifiers(PUBLIC, FINAL)
                .superclass(ParameterizedTypeName.get(TypeSafeDiagnosingMatcher.class, record))
                .addMethod(anyRecordFactoryMethod)
                .addMethod(describeToMethod)
                .addMethod(matchesSafelyMethod)
                .addMethod(privateConstructor)
                .build();

        return JavaFile.builder(target.getName(), matcherClass)
                .indent("    ")
                .skipJavaLangImports(true)
                .build().toString();
    }

}

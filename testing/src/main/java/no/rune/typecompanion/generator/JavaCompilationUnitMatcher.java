package no.rune.typecompanion.generator;

import com.google.testing.compile.Compilation.Status;
import com.google.testing.compile.JavaFileObjects;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.google.testing.compile.Compiler.javac;

public class JavaCompilationUnitMatcher extends TypeSafeDiagnosingMatcher<JavaCompilationUnit> {

    public static JavaCompilationUnitMatcher compiles() {
        return new JavaCompilationUnitMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a compilable unit");
    }

    @Override
    protected boolean matchesSafely(JavaCompilationUnit compilationUnit, Description mismatchDescription) {
        var compiledUnit = javac().compile(JavaFileObjects.forSourceString(compilationUnit.fullyQualifiedName(), compilationUnit.content()));
        boolean matches = true;
        if (compiledUnit.status() != Status.SUCCESS) {
            matches = false;
            mismatchDescription
                .appendText(" resulted in ").appendValue(compiledUnit.status())
                .appendText(" for ").appendText(compilationUnit.fullyQualifiedName())
                .appendValueList(":\n\n", "\n", "", compiledUnit.errors())
                .appendText("\n\nwhen trying to compile:\n\n")
                .appendText(compilationUnit.content());
        }
        return matches;
    }

    private JavaCompilationUnitMatcher() {
    }
}

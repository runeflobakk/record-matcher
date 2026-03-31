package no.rune.typecompanion;

import java.util.Optional;

public record JavaCompilationUnit(String content, String name, Optional<String> packageName) {

    public JavaCompilationUnit(String content, String name, Package location) {
        this(content, name, Optional.of(location).map(Package::getName));
    }

}

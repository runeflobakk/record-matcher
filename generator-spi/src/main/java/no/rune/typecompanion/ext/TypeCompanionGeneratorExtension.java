package no.rune.typecompanion.ext;

import no.rune.typecompanion.TypeCompanionGenerator;

public interface TypeCompanionGeneratorExtension {

    int requiredJavaMajorVersion();

    boolean applicableFor(Class<?> sourceType);

    TypeCompanionGenerator codeGenerator();

}

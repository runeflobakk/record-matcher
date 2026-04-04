package no.rune.typecompanion.generator.ext;

import no.rune.typecompanion.generator.TypeCompanionGenerator;

public interface TypeCompanionGeneratorExtension {

    int requiredJavaMajorVersion();

    boolean applicableFor(Class<?> sourceType);

    TypeCompanionGenerator codeGenerator();

}

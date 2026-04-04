package no.rune.typecompanion.generator;

public interface TypeCompanionGenerator {

    JavaCompilationUnit generateFor(Class<?> type);

}

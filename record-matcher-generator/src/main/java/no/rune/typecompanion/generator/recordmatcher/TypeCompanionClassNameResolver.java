package no.rune.typecompanion.generator.recordmatcher;

@FunctionalInterface
interface TypeCompanionClassNameResolver {

    String resolve(Class<?> type);

}

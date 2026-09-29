package no.rune.typecompanion.generator.sealedtype;

@FunctionalInterface
interface TypeCompanionClassNameResolver {

    String resolve(Class<?> type);

}

package no.rune.typecompanion.generator.recordmatcher;

@FunctionalInterface
interface RecordMatcherClassNameResolver {

    String resolve(Class<?> record);

}

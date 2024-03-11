package no.rune.record.matcher;

@FunctionalInterface
interface RecordMatcherClassNameResolver {

    String resolve(Class<? extends Record> record);

}

package no.rune.record;

@FunctionalInterface
public interface RecordMatcherClassNameResolver {

    String resolve(Class<? extends Record> record);

}

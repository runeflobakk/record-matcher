package no.rune.record;

final class DefaultRecordMatcherClassNameResolver implements RecordMatcherClassNameResolver {

    @Override
    public String resolve(Class<? extends Record> record) {
        var className = new StringBuilder(record.getSimpleName() + "Matcher");

        for (var enclosing = record.getEnclosingClass(); enclosing != null; enclosing = enclosing.getEnclosingClass()) {
            className.insert(0, enclosing.getSimpleName());
        }
        return className.toString();
    }

}

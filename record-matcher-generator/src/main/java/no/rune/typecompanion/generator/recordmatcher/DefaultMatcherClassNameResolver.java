package no.rune.typecompanion.generator.recordmatcher;

final class DefaultMatcherClassNameResolver implements RecordMatcherClassNameResolver {

    @Override
    public String resolve(Class<?> type) {
        var className = new StringBuilder(type.getSimpleName() + "Matcher");

        for (var enclosing = type.getEnclosingClass(); enclosing != null; enclosing = enclosing.getEnclosingClass()) {
            className.insert(0, enclosing.getSimpleName());
        }
        return className.toString();
    }

}

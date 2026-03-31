package no.rune.record.matcher;

import no.rune.typecompanion.ext.TypeCompanionGeneratorExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static no.rune.record.matcher.ScanHelper.isAccessibleFromSamePackage;

class RecordMatcherGeneratorExtension implements TypeCompanionGeneratorExtension {

    private static final Logger LOG = LoggerFactory.getLogger(RecordMatcherGeneratorExtension.class);

    @Override
    public RecordMatcherGenerator codeGenerator() {
        return new RecordMatcherGenerator();
    }

    @Override
    public int requiredJavaMajorVersion() {
        return 17;
    }

    @Override
    public boolean applicableFor(Class<?> sourceType) {
        return sourceType.isRecord()
                && isAccessibleFromSamePackage(sourceType)
                && isNonGeneric(sourceType);
    }

    private static boolean isNonGeneric(Class<?> type) {
        var typeParams = type.getTypeParameters();
        if (typeParams.length != 0) {
            LOG.debug("Not including {}<{}> because type parameters are not supported",
                    type.getName(), Stream.of(typeParams).map(t -> t.getName()).collect(joining(", ")));
            return false;
        }
        return true;
    }
}

package no.rune.typecompanion.generator.recordmatcher;

import com.google.auto.service.AutoService;
import java.util.stream.Stream;
import no.rune.typecompanion.generator.ext.TypeCompanionGeneratorExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;
import static no.rune.typecompanion.generator.recordmatcher.ScanHelper.isAccessibleFromSamePackage;

@AutoService(TypeCompanionGeneratorExtension.class)
public class RecordMatcherGeneratorExtension implements TypeCompanionGeneratorExtension {

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

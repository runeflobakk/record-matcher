package no.rune.typecompanion.generator.sealedtype;

import com.google.auto.service.AutoService;
import no.rune.typecompanion.generator.ext.TypeCompanionGeneratorExtension;

import static no.rune.typecompanion.generator.sealedtype.ScanHelper.isAccessibleFromSamePackage;

@AutoService(TypeCompanionGeneratorExtension.class)
public class SealedTypeMatcherGeneratorExtension implements TypeCompanionGeneratorExtension {

    @Override
    public int requiredJavaMajorVersion() {
        return 17;
    }

    @Override
    public boolean applicableFor(Class<?> sourceType) {
        return sourceType.isInterface() && sourceType.isSealed() && isAccessibleFromSamePackage(sourceType);
    }

    @Override
    public SealedTypeMatcherGenerator codeGenerator() {
        return new SealedTypeMatcherGenerator();
    }

}

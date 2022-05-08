package no.rune.record;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;


class RecordMatcherGeneratorTest {

    private final RecordMatcherGenerator generator = new RecordMatcherGenerator();

    @Test
    void canOnlyGenerateFromRecord() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromRecord(String.class));
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromRecord(CharSequence.class));
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromRecord(InputStream.class));
    }

}

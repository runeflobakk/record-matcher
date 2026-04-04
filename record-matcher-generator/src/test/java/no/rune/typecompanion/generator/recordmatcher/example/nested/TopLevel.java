package no.rune.typecompanion.generator.recordmatcher.example.nested;

public record TopLevel() {

    public record Nested(int value) {
        public record EvenMore() {}
    }
}

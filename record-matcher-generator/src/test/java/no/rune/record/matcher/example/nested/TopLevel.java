package no.rune.record.matcher.example.nested;

public record TopLevel() {

    public record Nested(int value) {
        public record EvenMore() {}
    }
}

package no.rune.typecompanion.generator.sealedtype.example.single;

public sealed interface SingleSubType {

    public record SingleImpl(String text) implements SingleSubType {}

}

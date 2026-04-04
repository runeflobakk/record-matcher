package no.rune.typecompanion.generator.recordmatcher.example.privateparts;

record PrivatePartsRecord(int number, Unaccessible unaccessible) {

    private class Unaccessible {
    }

}

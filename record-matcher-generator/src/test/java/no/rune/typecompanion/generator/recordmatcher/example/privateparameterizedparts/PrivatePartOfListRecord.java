package no.rune.typecompanion.generator.recordmatcher.example.privateparameterizedparts;

import java.util.List;

record PrivatePartOfListRecord(int number, List<Unaccessible> unaccessibles) {

    private class Unaccessible {
    }

}

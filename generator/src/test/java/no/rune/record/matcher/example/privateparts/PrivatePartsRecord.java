package no.rune.record.matcher.example.privateparts;

record PrivatePartsRecord(int number, Unaccessible unaccessible) {

    private class Unaccessible {
    }

}

package no.rune.text;

public final class Affixing {

    public static String withIndefArticle(String noun, Casing.Style style) {
        return Casing.to(style, indefArticleOf(noun), noun);
    }


    private static final String[] LIKELY_VOWEL_SOUND_PREFIXES = {"a", "A", "e", "E", "i", "I", "o", "O"};

    public static String indefArticleOf(String noun) {
        if (noun != null) {
            for (String likelyVowelSound : LIKELY_VOWEL_SOUND_PREFIXES) {
                if (noun.startsWith(likelyVowelSound)) {
                    return "an";
                }
            }
        }
        return "a";
    }

    private Affixing() {}
}

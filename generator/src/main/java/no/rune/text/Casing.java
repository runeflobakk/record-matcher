package no.rune.text;

import java.util.StringJoiner;
import java.util.function.IntUnaryOperator;

public final class Casing {

    public enum Style {
        camelCase((s, i) -> i == 0 ? mapCharAt(0, s, Character::toLowerCase) : mapCharAt(0, s, Character::toUpperCase)),
        PascalCase((s, i) -> mapCharAt(0, s, Character::toUpperCase)),
        snake_case((s, i) -> s.toLowerCase(), "_"),
        CAPITALIZED_SNAKE_CASE((s, i) -> s.toUpperCase(), "_");

        private interface ObjIntFunction<T> {
            T apply(T t, int i);
        }

        private final String delimiter;
        private final ObjIntFunction<String> tokenMapper;

        Style(ObjIntFunction<String> wordMapper) {
            this(wordMapper, "");
        }

        Style(ObjIntFunction<String> wordMapper, String delimiter) {
            this.delimiter = delimiter;
            this.tokenMapper = wordMapper;
        }


    }

    public static String to(Style style, String ... tokens) {
        StringJoiner joiner = new StringJoiner(style.delimiter);
        for (int i = 0; i < tokens.length; i++) {
            joiner.add(style.tokenMapper.apply(tokens[i], i));
        }
        return joiner.toString();
    }

    public static String mapCharAt(int index, String source, IntUnaryOperator fn) {
        char originalChar = source.charAt(index);
        char mappedChar = (char) fn.applyAsInt(originalChar);
        if (mappedChar == originalChar) {
            return source;
        } else {
            char[] chars = source.toCharArray();
            chars[index] = mappedChar;
            return new String(chars);
        }
    }

    private Casing() {
    }
}

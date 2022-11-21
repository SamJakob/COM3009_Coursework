package com.samjakob.com3009;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Crypto {

    /**
     * Gets the 'alphabetic base' of a given character. This is the first index (i.e., the letter A) in an alphabet's
     * character range.
     * <p>
     * e.g., if it's a capital letter, we're working with the capital letters range from 65 to 90,
     * so the alphabetic base would be 65.
     *
     * @param c The character to get the base for.
     * @return The ASCII code base for that character's alphabet.
     */
    private static int getAlphabeticBase(char c) {
        if (c >= 65 && c <= 90) return 65;      // Uppercase letters.
        if (c >= 97 && c <= 122) return 97;     // Lowercase letters.
        return 0;
    }

    /**
     * Adds the character code of character k to the character code of character c
     * @param c The character that should have another added to it. (Usually, from the plaintext string).
     * @param k The character that should be added to c. (Usually, from the key).
     * @param inverse Whether to perform an addition or a subtraction (the latter, used to retrieve the original
     *                value specified to an addition).
     * @return The new character.
     */
    private static char charDelta(char c, char k, boolean inverse) {
        int base = getAlphabeticBase(c);

        // If the plaintext character is in a recognized range, attempt to
        // perform the shift.
        if (base != 0) {
            int kBase = getAlphabeticBase(k), kOffset = 0;
            if (kBase != 0) kOffset = (k - kBase) % 26;

            if (inverse) kOffset = 26 - kOffset;

            // Add the k offset (from the start of the alphabet)
            // to the c offset (from the start of the alphabet)
            // and mod by 26 to ensure the new character remains
            // in the range of the alphabet by 'wrapping around'.
            return (char) (base + (((c - base) + kOffset) % 26));
        }

        // If we're here, C was not in a recognized range, so just return
        // C as-is.
        return c;
    }

    /**
     * A convenience alias for {@link #charDelta(char, char, boolean)} where inverse is set to false.
     * @see #charDelta(char, char, boolean)
     */
    public static char charAdd(char c, char k) {
        return charDelta(c, k, false);
    }

    public static List<Integer> stringDelta(String c, String k, boolean inverse) {
        final var aUpper = c.toUpperCase();
        final var bUpper = k.toUpperCase();

        return IntStream
                // Create a stream that generates characters from 0 to c.length (upper bound exclusive).
                .range(0, c.length())
                // Ensure the stream is parallelized.
                .parallel()
                // Map each character to the sum of the characters (mod 26).
                .map(i -> charDelta(aUpper.charAt(i), bUpper.charAt(i), inverse) - 65).boxed().toList();
    }

    public static List<Integer> stringSubtract(String a, String b) {
        return stringDelta(a, b, true);
    }

    public static List<Integer> stringAdd(String a, String b) {
        return stringDelta(a, b, false);
    }

    public static String vigenereEncrypt(String str, String key) {
        final var output = new StringBuilder();

        int counter = 0;
        for (char strChar : str.toCharArray()) {
            if (getAlphabeticBase(strChar) != 0) {
                output.append(charAdd(strChar, key.charAt(counter++ % key.length())));
            } else output.append(strChar);
        }

        return output.toString();
    }

    public static String alphabetIndexToString(List<Integer> input) {
        return collectAsString(input.stream().parallel().map(i -> i + 65));
    }

    public static String codePointsToString(List<Integer> input) {
        return collectAsString(input.stream().parallel());
    }

    public static String collectAsString(Stream<Integer> input) {
        return input
        // Ensure the stream is parallelized. Does nothing if the stream is already parallelized.
        .parallel()
        // Collect the stream of code points to a StringBuilder instance.
        // This works in parallel by making multiple instances of StringBuilder and then folding them into once
        // instance at the end with the combiner.
        .collect(
            // supplier -> initial value
            StringBuilder::new,
            // accumulator -> fold a new value into a container
            StringBuilder::appendCodePoint,
            // combiner -> fold two containers together
            StringBuilder::append)
        // Build the string from the StringBuilder instance.
        .toString();
    }

    public static char[][] toBlocks(String input, int blockSize) {
        if (blockSize % 8 != 0) throw new IllegalArgumentException("Block size must be divisible by 8");

        char[][] blocks = new char[input.length() / blockSize][blockSize];
        char[] inputCharacters = input.toCharArray();

        for (int i = 0; i < inputCharacters.length; i++) {
            blocks[i / blockSize][i % blockSize] = inputCharacters[i];
        }

        return blocks;
    }

    public static int computePaddingRequired(String input, int desiredLength) {
        return desiredLength - input.length();
    }

    public static String pad(String input, int padding) {
        String padChar = Integer.toHexString(padding).toUpperCase();
        if (padChar.length() == 1) padChar = "0" + padChar;
        return input + padChar.repeat(padding);
    }

}

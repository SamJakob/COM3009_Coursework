package com.samjakob.com3009;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.samjakob.com3009.Crypto.vigenereEncrypt;
import static com.samjakob.com3009.Utils.*;

public class VigenereCracker {

    private static final String ASSETS_DIRECTORY = "{{REDACTED}}";

    private static final String c1 = "TDTDINFRUU";
    private static final String c2 = "DKAKRNZXSI";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        long current = start;

        Supplier<Stream<String>> words = loadLines(
            "10letterwordslist.txt", ASSETS_DIRECTORY,
            // Filter lines by lines of length 10.
            line -> line.length() == 10
        );

        Supplier<Predicate<String>> fileHasWordComputer = (() -> {
            final List<String> cache;

            if (sealStreamAfter(words.get().parallel(), Stream::count) < 10_000_000) {
                cache = sealStreamAfter(words.get().parallel(), Stream::toList);
                return cache::contains;
            } else {
                return word -> sealStreamAfter(words.get().parallel(), s -> s.anyMatch(otherWord -> otherWord.equals(word)));
            }
        });

        Predicate<String> fileHasWord = fileHasWordComputer.get();
        current = benchmark(start, "Loaded words!", current);

        var cipherDiffRaw = Crypto.stringSubtract(c1, c2);
        String cipherDiff = Crypto.alphabetIndexToString(cipherDiffRaw);
        current = benchmark(start, String.format("Computed cipher text diff: %s", cipherDiff), current);

        AtomicReference<String> w1Ref = new AtomicReference<>();
        String w2 = sealStreamAfter(words.get().parallel(), s ->
                // We assume there exists a word in the list, w2, such that w2 + (c1 - c2) = w1, another word in the
                // list, so...
                s.filter(word -> {
                    // Check if there exists some word in the words list that matches the current word with the
                    // cipher text diff added to it.
                    String potentialW1 = Crypto.alphabetIndexToString(Crypto.stringAdd(word, cipherDiff));

                    if (fileHasWord.test(potentialW1)) {
                        w1Ref.set(potentialW1);
                        System.out.println("Candidate for w1 is: " + potentialW1); return true;
                    }

                    else return false;
                })
                // As it is assumed present, find a match and obtain it - otherwise throw NoSuchElementException.
                // In this case, we assume there is only one viable candidate that satisfies the problem, but this code
                // could be refactored to search all matches, should this assumption not be the case.
                .findAny().orElseThrow());
        System.out.println("Associate for w1 is: " + w2 + " (candidate for w2)");
        current = benchmark(start, "Obtained candidates for w1 and w2.", current);

        System.out.println();
        String w1 = w1Ref.get();

        // Perform a final check and print the key if it can thus be found, otherwise exit with an error message.
        System.out.print("Confirming w1 - c1 == w2 - c2: ");
        if (!Crypto.stringSubtract(w1, c1).equals(Crypto.stringSubtract(w2, c2))) {
            System.out.println("Nope");
            throw new RuntimeException("The code must be amended: the wrong word was selected for a given cipher text.");
        }

        System.out.println("Yep");

        String k = Crypto.alphabetIndexToString(Crypto.stringSubtract(c1, w1));
        System.out.println("Thus, the key is: " + k);
        System.out.println();

        System.out.println("Performing forwards encryption of found plain texts with key to verify...");

        boolean checkText1 = vigenereEncrypt(w1, k).equals(c1);
        System.out.printf("Checking that (w1 (%s) + k (%s)) mod 26 = c1 (%s)...\t\t\t[%s]%n", w1, k, c1, checkText1 ? "PASS" : "FAIL");

        boolean checkText2 = vigenereEncrypt(w2, k).equals(c2);
        System.out.printf("Checking that (w2 (%s) + k (%s)) mod 26 = c2 (%s)...\t\t\t[%s]%n", w2, k, c2, checkText2 ? "PASS" : "FAIL");

        if (!(checkText1 && checkText2)) {
            throw new RuntimeException("The code must be amended: the plain texts were not coherent with the key and cipher texts.");
        }

        benchmark(start, "Successfully verified found plain texts", current);

        System.out.println();
        System.out.println("============= [ Results ] =============");
        System.out.printf("w1\t=\t%s\t(plain text for c1)%n", w1);
        System.out.printf("w2\t=\t%s\t(plain text for c2)%n", w2);
        System.out.printf("k\t=\t%s\t(key)%n", k);
        System.out.println();
    }

}

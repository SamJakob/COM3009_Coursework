package com.samjakob.com3009;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.samjakob.com3009.Utils.benchmark;
import static com.samjakob.com3009.Utils.loadLines;

public class RSACracker {

    private static final String CIPHER_TEXT = "1696D782AA667030E0674FCD619F6D582F776AEE0A1F2D0C436C1CE82E09C7878CA00D8729FFEFBB6FF215258A03D2E0AFEC3AD0D1F7E99608E0D1B9F2FC5E79";
    private static final String ASSETS_DIRECTORY = "{{REDACTED}}";

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        long start = System.currentTimeMillis();
        long current = start;

        final var modulus = new BigInteger("8062109139697837412010302777489525789867989267700726363070582369964827475935728103493573989386020363966737452299551740667409867231826257650621004615946443");
        final var exponent = new BigInteger("65537");

        final var publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
        final var factory = KeyFactory.getInstance("RSA");
        final var publicKey = factory.generatePublic(publicKeySpec);

        current = benchmark(start, "Initializing public key and key factory.", current);

        Supplier<Stream<String>> words = loadLines(
            "english.txt", ASSETS_DIRECTORY,
            // Filter lines by lines of length 10.
            line -> line.length() > 0
        );

        current = benchmark(start, "Loaded file.", current);

        final byte[] rawCipherText = HexFormat.of().parseHex(CIPHER_TEXT);

        final var cipher = Cipher.getInstance("RSA/ECB/NoPadding");

        Supplier<Cipher> cipherSupplier = () -> {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher;
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        };

        AtomicInteger hashesChecked = new AtomicInteger(0);

        final String result = words.get().sequential().filter(word -> {
            try {
                hashesChecked.addAndGet(1);
                return Arrays.equals(
                    cipherSupplier.get().doFinal(word.getBytes(StandardCharsets.UTF_8)),
                    rawCipherText
                );
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }).findAny().orElseThrow();

        current = benchmark(start, "Found result (checked " + hashesChecked.get() + " hash(es))!", current);
        System.out.println();
        System.out.println("Word is: " + result);
    }

}

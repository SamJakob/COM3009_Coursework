package com.samjakob.com3009;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.stream.LongStream;

import static com.samjakob.com3009.Utils.benchmark;

public class AESBenchmark {

    private static final String ZERO_VALUE = "0000000000000000";
    private static final long HASH_COUNT = (long) Math.pow(2, 16);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        long current = start;

        current = benchmark(start, "Ready to begin...", current);

        // Unoptimized code to encrypt 100 times.
        LongStream.range(0, HASH_COUNT).sequential().forEach(i -> encrypt());

        current = benchmark(start, String.format("Computed %,d hashes", HASH_COUNT), current);
    }

    private static void encrypt() {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(128), generateIv());
            byte[] cipherText = cipher.doFinal(AESBenchmark.ZERO_VALUE.getBytes(Charset.defaultCharset()));
            HexFormat.of().formatHex(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static SecretKey generateKey(int size) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(size);
        return generator.generateKey();
    }

    private static IvParameterSpec generateIv() {
        final byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

}

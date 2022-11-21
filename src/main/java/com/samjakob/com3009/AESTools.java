package com.samjakob.com3009;

import static com.samjakob.com3009.Crypto.*;
import static com.samjakob.com3009.Utils.printBlocks;

public class AESTools {

    /*
    KEY:    4a c4 27 e1 f5 71 1f 60 8b a9 63 55 6a 70 3a c1
    IV:     74 c3 ee 79 17 20 f1 38 a8 c3 19 4d 13 17 73 20
     */
//    private static final String KNOWN_CIPHER = "ACC36D24CE29720496535023F651DD4D80BA9D70554DB8E05C3BB8A11991775103D6B9AB731038ED2CCC89F02DBF7CC1E07F70AE4643BF88F7F3EE339CEE3637";

    private static final String KNOWN_CIPHER = "EE7B2A581995D61CE866432A53F1A0651E368F73E8FF22C61F3C51E64887649B2DDF68BF09950C477D015A788B5660E94753245B83294967E6AC26D4872CB487";

    private static final String PLAIN_TEXT_HEX = "506C65617365206D656574206D65206174204775696C64666F72642053746174696F6E3A20373A30302062652070726F6D707421";

    private static final String PLAIN_TEXT = "Please meet me at Guildford Station: 7:00 be prompt!";

    private static final String NEW_BLOCK = "1E368F73E8F022C51F3C51E64887649B";
    //                                                                              |                               |
    //                                              "EE7B2A581995D61CE866432A53F1A0651E368F73E8FF22C61F3C51E64887649B2DDF68BF09950C477D015A788B5660E94753245B83294967E6AC26D4872CB487";
    private static final String CHANGED_CIPHER =    "EE7B2A581995D61CE866432A53F1A0651E368F73E8F022C51F3C51E64887649B2DDF68BF09950C477D015A788B5660E94753245B83294967E6AC26D4872CB487";

    private static final int AES_BLOCK_SIZE = (128 / 8) * 2;

    // Simple utility for printing a string in formatted blocks for visualization purposes.
    public static void main(String[] args) {
        printBlocks(toBlocks(CHANGED_CIPHER, AES_BLOCK_SIZE));    // An AES block is 128-bit.

        int padding = computePaddingRequired(PLAIN_TEXT_HEX, 128) / 2;

        System.out.printf("Needs %x padding bits.%n", padding);
        printBlocks(toBlocks(pad(PLAIN_TEXT_HEX, padding), AES_BLOCK_SIZE));

        char[] plain = PLAIN_TEXT.toCharArray();
        for (int i = 0; i < PLAIN_TEXT.length(); i++) {
            System.out.print((i % 16 == 0 && i != 0 ? "-|" : "") + plain[i] + " |");
        }
        System.out.println();

//        printBlocks(toBlocks(PLAIN_TEXT, 128 / 8));
    }

}

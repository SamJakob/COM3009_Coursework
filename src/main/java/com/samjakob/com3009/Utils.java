package com.samjakob.com3009;

import com.samjakob.com3009.functional_extensions.RiskyConsumer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Utils {

    public static Supplier<Stream<String>> loadLines(String path) {
        return loadLines(path, null, null);
    }

    public static Supplier<Stream<String>> loadLines(String path, Predicate<String> filter) {
        return loadLines(path, null, filter);
    }

    public static Supplier<Stream<String>> loadLines(String path, String baseDirectory) {
        return loadLines(path, baseDirectory, null);
    }

    /**
     * Loads the lines from a file, optionally filtering by the specified filter if it is not null.
     * A supplier is returned to allow this to be called on demand by code requiring the word list.
     *
     * @param file The name of the file to load. Optionally, with a baseDirectory prepended.
     * @param baseDirectory If specified, is prepended to file.
     * @param filter If specified, a filter that is applied to each line of the file.
     * @return The stream of words from the file.
     */
    public static Supplier<Stream<String>> loadLines(String file, String baseDirectory, Predicate<String> filter) {
        Path filePath = Paths.get(
            baseDirectory != null
                ? baseDirectory + File.separator + file
                : file);

        return () -> {
            // We mustn't try-with-resources here, or close, as regardless of what happens here the stream is to be
            // passed on and used later (which isn't particularly useful if the stream is already closed), thus we
            // suppress the inspection.
            //noinspection resource
            Stream<String> lineStream = RiskyConsumer.wrap((Path x) -> Files.lines(x).parallel()).apply(filePath);
            if (filter != null) lineStream = lineStream.filter(filter);
            return lineStream;
        };
    }

    /**
     * Applies the specified action to the specified stream, sealing the stream afterwards by calling close. The result
     * of action is then proxied and returned by this function.
     * @param stream The stream to act on.
     * @param action The action to perform to the stream.
     * @return The return value of the action.
     * @param <T> The parameter for the stream type.
     * @param <R> The result (return type) of the action and therefore of this function.
     */
    public static <T, R> R sealStreamAfter(Stream<T> stream, Function<Stream<T>, R> action) {
        final R result = action.apply(stream);
        stream.close();
        return result;
    }

    public static long benchmark(long startTime, String message, long sinceLast) {
        long endTime = System.currentTimeMillis();
        System.out.printf("(+%d ms) %s (took %d ms)%n", endTime - startTime, message, endTime - sinceLast);
        return endTime;
    }

    public static void printBlocks(char[][] blocks) {
        for (char[] block : blocks) {
            int i = 0;
            for (char c : block) {
                if (i != 0 && i % 2 == 0) System.out.print("|");
                System.out.print(c);
                i++;
            }
            System.out.print("|-|");
        }
        System.out.println();
    }

}

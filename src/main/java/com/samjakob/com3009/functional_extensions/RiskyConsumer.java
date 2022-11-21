package com.samjakob.com3009.functional_extensions;

import java.util.function.Function;

/**
 * Exposes a consumer interface that might throw an exception.
 */
@FunctionalInterface
public interface RiskyConsumer<T, U, E extends Exception> {
    U consume(T t) throws E;

    static <T, U> Function<T, U> wrap(RiskyConsumer<T, U, Exception> consumer) {
        return x -> { try { return consumer.consume(x); } catch (Exception e) { throw new RuntimeException(e); } };
    }
}


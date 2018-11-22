package finergit.rewrite;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * A tiny helper for converting IOException to UncheckedIOException.
 */
public class Try {
    @FunctionalInterface
    public static interface ThrowableRunnable {
        void run() throws IOException;
    }

    @FunctionalInterface
    public static interface ThrowableSupplier<T> {
        T get() throws IOException;
    }

    @FunctionalInterface
    public static interface ThrowableFunction<T, R> {
        R apply(T t) throws IOException;
    }

    public static void io(final ThrowableRunnable f) {
        try {
            f.run();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T io(final ThrowableSupplier<T> f) {
        try {
            return f.get();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T, R> Function<T, R> io(final ThrowableFunction<T, R> f) {
        return (x) -> {
            try {
                return f.apply(x);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}

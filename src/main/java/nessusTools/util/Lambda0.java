package nessusTools.util;

/**
 * Zero-argument lambda with a returned value
 *
 * Type parameters:
 *
 * R  : return -- the type of the return value from the Lambda1
 */

public interface Lambda0<R> {
    R call();

    public static abstract class NothingThrown extends RuntimeException {
        private NothingThrown() { }
    }
}

package nessusTools.sync;

/**
 * Type parameters:
 *
 * A  : argument -- the type of the argument to be passed to the run method
 *
 * R  : return -- the type of the return value from the Lambda
 */

public interface Lambda<A, R> {
    R call(A arg);

    public static abstract class NothingThrown extends RuntimeException {
        private NothingThrown() { }
    }
}

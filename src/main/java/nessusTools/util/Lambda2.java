package nessusTools.util;

/**
 * Two-argument lambda with a return value
 *
 * Type parameters:
 *
 * A  : argument1 -- the type of the first argument to be passed to the run method
 *
 * B  : argument2 -- the type of the second argument to be passed to the run method
 *
 * R  : return -- the type of the return value from the Lambda2
 */

public interface Lambda2<A, B, R> {
    R call(A arg1, B arg2);

    public static abstract class NothingThrown extends RuntimeException {
        private NothingThrown() { }
    }
}

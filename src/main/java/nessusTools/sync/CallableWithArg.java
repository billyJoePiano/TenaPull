package nessusTools.sync;

/**
 * Type parameters:
 *
 * A  : argument -- the type of the argument to be passed to the run method
 *
 * R  : return -- the type of the return value from the CallableWithArg
 *
 * E  : Exception/Error -- the type of any throwable which the callableWithArg might throw.
 * If no exceptions will be thrown, then use the static inner class NothingThrown as
 * the type for E. NothingThrown cannot be instantiated, and because it extends RuntimeException
 * it does not need to be caught or declared
*/

public interface CallableWithArg<A, R, E extends Throwable> {
    // Runnable with argument, return value, and throwable
    R run(A arg) throws E;

    public static abstract class NothingThrown extends RuntimeException {
        private NothingThrown() { }
    }
}

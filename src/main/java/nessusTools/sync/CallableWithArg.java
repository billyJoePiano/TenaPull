package nessusTools.sync;

public interface CallableWithArg<A, R, E extends Throwable> {
    // Runnable with argument, return value, and throwable
    R run(A arg) throws E;

    public static abstract class NothingThrown extends RuntimeException {
        private NothingThrown() { }
    }
}

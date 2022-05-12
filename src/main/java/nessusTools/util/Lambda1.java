package nessusTools.util;

/**
 * One-argument lambda with a return value
 *
 * @param <A> argument -- the type of the argument to be passed to the call method
 * @param <R> return -- the type of the return value from the Lambda1
 */
public interface Lambda1<A, R> {
    /**
     * Calls the lambda
     *
     * @param arg the argument to pass to the lambda
     * @return the return value from the lambda
     */
    R call(A arg);
}

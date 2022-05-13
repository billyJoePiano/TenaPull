package tenapull.util;

/**
 * Two-argument lambda with a return value
 *
 * @param <A> argument1 -- the type of the first argument to be passed to the call method
 * @param <B> argument2 -- the type of the second argument to be passed to the call method
 * @param <R> the return type
 */
public interface Lambda2<A, B, R> {
    /**
     * Call the lambda
     *
     * @param arg1 the first argument to the lambda
     * @param arg2 the second argument to the lambda
     * @return the return value from the lambda
     */
    R call(A arg1, B arg2);
}

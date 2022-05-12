package nessusTools.util;

/**
 * Zero-argument lambda with a returned value
 *
 * @param <R> return -- the type of the return value from the Lambda1
 */
public interface Lambda0<R> {
    /**
     * Calls the lambda
     *
     * @return the return value of the lambda
     */
    R call();
}

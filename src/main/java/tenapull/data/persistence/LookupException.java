package tenapull.data.persistence;


/**
 * Exception thrown when there is unexpected behavior during a lookup dao
 * operation
 */
public class LookupException extends DbException {
    /**
     * Instantiates a new Lookup exception.
     *
     * @param msg      the msg
     * @param pojoType the pojo type
     */
    public LookupException(String msg, final Class pojoType) {
        super(msg, pojoType);
    }

    /**
     * Instantiates a new Lookup exception.
     *
     * @param msg       the msg
     * @param rootCause the root cause
     * @param pojoType  the pojo type
     */
    public LookupException(String msg, Throwable rootCause, final Class pojoType) {
        super(msg, rootCause, pojoType);
    }

    /**
     * Instantiates a new Lookup exception.
     *
     * @param rootCause the root cause
     * @param pojoType  the pojo type
     */
    public LookupException(Throwable rootCause, final Class pojoType) {
        super(rootCause, pojoType);
    }
}
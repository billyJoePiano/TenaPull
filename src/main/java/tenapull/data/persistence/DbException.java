package tenapull.data.persistence;

/**
 * The type Db exception.
 */
public class DbException extends RuntimeException {
    private final Class pojoType;

    /**
     * Instantiates a new Db exception.
     *
     * @param msg      the msg
     * @param pojoType the pojo type
     */
    public DbException(String msg, final Class pojoType) {
        super(msg + " (for pojo type " + pojoType.toString() + ")");
        this.pojoType = pojoType;
    }

    /**
     * Instantiates a new Db exception.
     *
     * @param msg       the msg
     * @param rootCause the root cause
     * @param pojoType  the pojo type
     */
    public DbException(String msg, Throwable rootCause, final Class pojoType) {
        super(msg + " (For pojo type " + pojoType.toString() + ")", rootCause);
        this.pojoType = pojoType;
    }

    /**
     * Instantiates a new Db exception.
     *
     * @param rootCause the root cause
     * @param pojoType  the pojo type
     */
    public DbException(Throwable rootCause, final Class pojoType) {
        this("", rootCause, pojoType);
    }

    /**
     * Gets pojo type.
     *
     * @return the pojo type
     */
    public Class getPojoType() {
        return this.pojoType;
    }
}
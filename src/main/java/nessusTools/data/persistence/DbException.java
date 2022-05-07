package nessusTools.data.persistence;

public class DbException extends RuntimeException {
    private final Class pojoType;

    public DbException(String msg, final Class pojoType) {
        super(msg + " (for pojo type " + pojoType.toString() + ")");
        this.pojoType = pojoType;
    }

    public DbException(String msg, Throwable rootCause, final Class pojoType) {
        super(msg + " (For pojo type " + pojoType.toString() + ")", rootCause);
        this.pojoType = pojoType;
    }

    public DbException(Throwable rootCause, final Class pojoType) {
        this("", rootCause, pojoType);
    }

    public Class getPojoType() {
        return this.pojoType;
    }
}
package nessusTools.data.persistence;

public class LookupException extends RuntimeException {
    private final Class pojoType;

    public LookupException(String msg, final Class pojoType) {
        super(msg + " (for pojo type " + pojoType.toString() + ")");
        this.pojoType = pojoType;
    }

    public LookupException(String msg, Throwable rootCause, final Class pojoType) {
        super(msg + " (for pojo type " + pojoType.toString() + ")", rootCause);
        this.pojoType = pojoType;
    }

    public LookupException(Throwable rootCause, final Class pojoType) {
        this("", rootCause, pojoType);
    }

    public Class getPojoType() {
        return this.pojoType;
    }
}
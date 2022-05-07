package nessusTools.data.persistence;

public class LookupException extends DbException {
    public LookupException(String msg, final Class pojoType) {
        super(msg, pojoType);
    }

    public LookupException(String msg, Throwable rootCause, final Class pojoType) {
        super(msg, rootCause, pojoType);
    }

    public LookupException(Throwable rootCause, final Class pojoType) {
        super(rootCause, pojoType);
    }
}
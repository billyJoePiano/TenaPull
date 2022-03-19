package nessusTools.data.persistence;

public class LookupException extends Exception {
    private final Class pojoClass;

    public LookupException(String msg, final Class pojoClass) {
        super(msg + " (for pojo class " + pojoClass.toString() + ")");
        this.pojoClass = pojoClass;
    }

    public LookupException(String msg, Throwable rootCause, final Class pojoClass) {
        super(msg + " (for pojo class " + pojoClass.toString() + ")", rootCause);
        this.pojoClass = pojoClass;
    }

    public LookupException(Throwable rootCause, final Class pojoClass) {
        this("", rootCause, pojoClass);
    }

    public Class getPojoClass() {
        return this.pojoClass;
    }
}
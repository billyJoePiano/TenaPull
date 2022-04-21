package nessusTools.util;

/**
 * Wraps a value of any type (T).  Used so that a lambda may 'mutate' the value
 * of a variable in the outer scope, because lambdas require that an outer variable
 * be 'final or effectively final' to be accessible from within the lambda.  An
 * instance of var satisfies this requirement while allowing its 'value' property
 * to be mutated.
 *
 * Also includes static-inner-class wrappers of each primitive type
 *
 * @param <T>
 */
public class Var<T> {
    public T value;

    public Var() { }

    public Var(T initialValue) { this.value = initialValue; }


    public static class Bool {
        public boolean value;

        public Bool() { }

        public Bool(boolean initialValue) {
            value = initialValue;
        }
    }

    public static class Int {
        public int value;

        public Int() { }

        public Int(int initialValue) {
            this.value = initialValue;
        }
    }

    public static class String extends Var<String> {
        public String() { }

        public String(String initialValue) {
            super(initialValue);
        }
    }

    public static class Double {
        public double value;

        public Double() { }

        public Double(double initialValue) {
            this.value = initialValue;
        }
    }

    public static class Char {
        public char value;

        public Char() { }

        public Char(char initialValue) {
            this.value = initialValue;
        }
    }

    public static class Byte {
        public byte value;

        public Byte() { }

        public Byte(byte initialValue) {
            this.value = initialValue;
        }
    }

    public static class Long {
        public long value;

        public Long() { }

        public Long(long initialValue) {
            this.value = initialValue;
        }
    }

    public static class Float {
        public float value;

        public Float() { }

        public Float(float initialValue) {
            this.value = initialValue;
        }
    }

    public static class Short {
        public short value;

        public Short() { }

        public Short(short initialValue) {
            this.value = initialValue;
        }
    }
}

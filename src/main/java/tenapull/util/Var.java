package tenapull.util;

/**
 * Wraps a value of any type (T).  Used mainly so that a lambda may mutate the value
 * of a variable in the outer scope, because lambdas require that an outer variable
 * be 'final or effectively final' if it is to be accessible from within the lambda.
 * An instance of var satisfies this requirement while allowing its 'value' property
 * to be mutated.
 * <p>
 * Also includes static-inner-class wrappers of each primitive type
 *
 * @param <T> the type being wrapped
 */
public class Var<T> {
    /**
     * The value this Var wraps
     */
    public T value;

    /**
     * Instantiates a new Var with a default value of null
     */
    public Var() { }

    /**
     * Instantiates a new Var with the provided initial value
     *
     * @param initialValue the initial value
     */
    public Var(T initialValue) { this.value = initialValue; }


    /**
     * Var which wraps a boolean primitive
     */
    public static class Bool {
        /**
         * The value this Var wraps
         */
        public boolean value;

        /**
         * Instantiates a new Bool.
         */
        public Bool() { }

        /**
         * Instantiates a new Bool.
         *
         * @param initialValue the initial value
         */
        public Bool(boolean initialValue) {
            value = initialValue;
        }
    }

    /**
     * Var which wraps an int primitive
     */
    public static class Int {
        /**
         * The value this Var wraps
         */
        public int value;

        /**
         * Instantiates a new Int.
         */
        public Int() { }

        /**
         * Instantiates a new Int.
         *
         * @param initialValue the initial value
         */
        public Int(int initialValue) {
            this.value = initialValue;
        }
    }

    /**
     * Var which wraps a String
     */
    public static class String extends Var<String> {
        /**
         * Instantiates a new String.
         */
        public String() { }

        /**
         * Instantiates a new String.
         *
         * @param initialValue the initial value
         */
        public String(String initialValue) {
            super(initialValue);
        }
    }

    /**
     * Var which wraps a double primitive
     */
    public static class Double {
        /**
         * The value this Var wraps
         */
        public double value;

        /**
         * Instantiates a new Double.
         */
        public Double() { }

        /**
         * Instantiates a new Double.
         *
         * @param initialValue the initial value
         */
        public Double(double initialValue) {
            this.value = initialValue;
        }
    }

    /**
     * Var which wraps a char primitive
     */
    public static class Char {
        /**
         * The value this Var wraps
         */
        public char value;

        /**
         * Instantiates a new Char.
         */
        public Char() { }

        /**
         * Instantiates a new Char.
         *
         * @param initialValue the initial value
         */
        public Char(char initialValue) {
            this.value = initialValue;
        }
    }

    /**
     * Var which wraps a byte primitive
     */
    public static class Byte {
        /**
         * The value this Var wraps
         */
        public byte value;

        /**
         * Instantiates a new Byte.
         */
        public Byte() { }

        /**
         * Instantiates a new Byte.
         *
         * @param initialValue the initial value
         */
        public Byte(byte initialValue) {
            this.value = initialValue;
        }
    }

    /**
     * Var which wraps a long primitive
     */
    public static class Long {
        /**
         * The value this Var wraps
         */
        public long value;

        /**
         * Instantiates a new Long.
         */
        public Long() { }

        /**
         * Instantiates a new Long.
         *
         * @param initialValue the initial value
         */
        public Long(long initialValue) {
            this.value = initialValue;
        }
    }

    /**
     * Var which wraps a float primitive
     */
    public static class Float {
        /**
         * The value this Var wraps
         */
        public float value;

        /**
         * Instantiates a new Float.
         */
        public Float() { }

        /**
         * Instantiates a new Float.
         *
         * @param initialValue the initial value
         */
        public Float(float initialValue) {
            this.value = initialValue;
        }
    }

    /**
     * Var which wraps a short primitive
     */
    public static class Short {
        /**
         * The value this Var wraps
         */
        public short value;

        /**
         * Instantiates a new Short.
         */
        public Short() { }

        /**
         * Instantiates a new Short.
         *
         * @param initialValue the initial value
         */
        public Short(short initialValue) {
            this.value = initialValue;
        }
    }
}

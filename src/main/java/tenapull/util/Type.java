package tenapull.util;

import com.sun.istack.*;

import java.util.*;


/**
 * Represents a type that may have type parameters, including
 * any arbitrary level of nested type parameters
 * e.g. ReadWriteLock&lt;Map&lt;Set&lt;Thread&gt;, Dao&lt;Scan&gt;&gt;, List&lt;Scan&gt;&gt;.
 *
 * This is an immutable class.  To represent nested type params, pass a Type instance
 *  into the constructor of another Type instance
 *
 * @param <T> the base class which this type represents
 */
public final class Type<T> {
    // http://gafter.blogspot.com/2006/12/super-type-tokens.html
    // represents a class/type that can include type parameters at runtime

    private final Class<T> type;
    private final Type[] params;

    /**
     * Instantiates a new Type for the provided class
     *
     * @param type the type
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type) throws NullPointerException {
        this.type = type;
        this.params = null;
    }

    /**
     * Instantiates a new Type for the provided class, with the given class
     * as a type param
     *
     * @param type  the type
     * @param param the param
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type, Class param)
            throws NullPointerException {

        this.type = type;
        this.params = new Type[] { new Type(param) };
    }

    /**
     * Instantiates a new Type for the provided class, with the given Type
     * as a type param
     *
     * @param type  the type
     * @param param the param
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type, Type param)
            throws NullPointerException {

        this.type = type;
        if (type == null) throw new NullPointerException();
        this.params = new Type[] { param };
    }

    /**
     * Instantiates a new Type for the provided class, with two type params
     * of the two provided classes
     *
     * @param type   the type
     * @param param1 the param 1
     * @param param2 the param 2
     */
    public Type(Class<T> type,
                Class param1,
                Class param2) {
        this.type = type;
        this.params = new Type[] { new Type(param1), new Type(param2) };
    }

    /**
     * Instantiates a new Type for the provided class, with the two type params
     * of the two provided types
     *
     * @param type   the type
     * @param param1 the param 1
     * @param param2 the param 2
     */
    public Type(Class<T> type,
                Type param1,
                Type param2) {

        this.type = type;
        if (param1 == null || param2 == null) throw new NullPointerException();
        this.params = new Type[] { param1, param2 };
    }


    /**
     * Instantiates a new Type for the provided class, with an array
     * of type params
     *
     * @param type   the type
     * @param params the params
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type, Type[] params)
            throws NullPointerException {

        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;

        if (params == null) {
            this.params = null;
            return;
        }

        params = params.clone();

        for (Type param : params) {
            if (param == null) throw new NullPointerException();
        }

        this.params = params;

    }

    /**
     * Instantiates a new Type for the provided class, with an array of
     * classes as type params
     *
     * @param type   the type
     * @param params the params
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type, Class[] params)
            throws NullPointerException {

        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;

        if (params == null) {
            this.params = null;
            return;
        }

        this.params = new Type[params.length];

        for (int i = 0; i < params.length; i++) {
            this.params[i] = new Type(params[i]);
        }
    }

    /**
     * Instantiates a new Type for the provided class with a List
     * of classes as the type params
     *
     * @param type   the type
     * @param params the params
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type,
                @Nullable List<Class> params)
            throws NullPointerException {

        this(type, (Class[]) params.toArray());
    }

    /**
     * Instantiates a new Type for the provided class with a list
     * of types as the type params
     *
     * @param type               the type
     * @param params             the params
     * @param nullDifferentiator the null differentiator
     * @throws NullPointerException the null pointer exception
     */
    public Type(Class<T> type,
                List<Type> params,
                Void nullDifferentiator) //because type erasure is otherwise the same as the previous constructor
            throws NullPointerException {

        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;

        if (params == null) {
            this.params = null;
            return;
        }

        this.params = (Type[]) params.toArray().clone();

        for (Type param : this.params) {
            if (param == null) throw new NullPointerException();
        }
    }

    /**
     * Gets root class this type represents
     *
     * @return the type
     */
    public Class<T> getType() {
        return this.type;
    }

    /**
     * Gets a copy of the array of type params, or null if there are none
     *
     * @return the type [ ]
     */
    public Type[] getParams() {
        if (this.params == null) {
            return null;
        }
        return this.params.clone();
    }


    public int hashCode() {
        int code = this.type.hashCode();

        if (this.params != null)
        for (Type type : this.params) {
            code ^= this.params.hashCode();
        }
        return code;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Type)) {
            return false;
        }

        Type other = (Type) o;

        return Objects.equals(this.type, other.type)
                && (this.params == null
                ? other.params == null
                : other.params != null
                && Arrays.equals(this.params, other.params));
    }

    public String toString() {
        return "(Type)<" + this.toStringBasic() + ">";
    }

    /**
     * Produces a string representation of this type (with type params if applicable)
     * that does NOT include an indication that this represents an instance of the Type class.
     * Used mainly by recursive calls within Type, and also by KeyValueType
     *
     * @return the string
     */
    public String toStringBasic() {
        String str = this.type.getSimpleName();
        if (this.params == null) {
            return str;
        }

        str += "<";
        boolean firstIteration = true;

        for (Type param : params) {
            if (firstIteration) {
                firstIteration = true;
            } else {
                str += ", ";
            }

            str += param.toStringBasic();
        }

        return str + ">";
    }
}

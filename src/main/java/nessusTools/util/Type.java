package nessusTools.util;

import com.sun.istack.*;

import java.util.*;


public final class Type<T> {
    // http://gafter.blogspot.com/2006/12/super-type-tokens.html
    // represents a class/type that can include type parameters at runtime

    private final Class<T> type;
    private final Type[] params;

    public Type(Class<T> type) throws NullPointerException {
        this.type = type;
        this.params = null;
    }

    public Type(Class<T> type, Class param)
            throws NullPointerException {

        this.type = type;
        this.params = new Type[] { new Type(param) };
    }

    public Type(Class<T> type, Type param)
            throws NullPointerException {

        this.type = type;
        if (type == null) throw new NullPointerException();
        this.params = new Type[] { param };
    }

    public Type(Class<T> type,
                Class param1,
                Class param2) {
        this.type = type;
        this.params = new Type[] { new Type(param1), new Type(param2) };
    }

    public Type(Class<T> type,
                Type param1,
                Type param2) {

        this.type = type;
        if (param1 == null || param2 == null) throw new NullPointerException();
        this.params = new Type[] { param1, param2 };
    }


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

    public Type(Class<T> type,
                @Nullable List<Class> params)
            throws NullPointerException {

        this(type, (Class[]) params.toArray());
    }

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

    public Class<T> getType() {
        return this.type;
    }

    public Type[] getParams() {
        if (this.params == null) {
            return null;
        }
        return this.params.clone();
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

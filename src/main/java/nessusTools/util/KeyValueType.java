package nessusTools.util;

import com.sun.istack.*;
import nessusTools.sync.*;

import java.util.*;

public class KeyValueType<K, V> {
    private Type<K> keyType;
    private Type<V> valueType;

    public KeyValueType(Type<K> keyType, Type<V> valueType)
            throws NullPointerException {
        
        if (keyType == null || valueType == null) {
            throw new NullPointerException();
        }
        
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public KeyValueType(Class<K> keyType, Class<V> valueType)
            throws NullPointerException {

        this.keyType = new Type(keyType);
        this.valueType = new Type(valueType);
    }

    public KeyValueType(Class<K> keyType, Type<V> valueType)
            throws NullPointerException {

        if (valueType == null) {
            throw new NullPointerException();
        }

        this.keyType = new Type(keyType);
        this.valueType = valueType;
    }

    public KeyValueType(Type<K> keyType, Class<V> valueType)
            throws NullPointerException {

        if (keyType == null) {
            throw new NullPointerException();
        }

        this.keyType = keyType;
        this.valueType = new Type(valueType);
    }

    public Type<K> getKeyType() {
        return this.keyType;
    }

    public Type<V> getValueType() {
        return this.valueType;
    }

    public String toString() {
        return "<" + this.keyType + ", " + this.valueType + ">";
    }
    

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }
        KeyValueType other = (KeyValueType) o;
        return Objects.equals(this.keyType, other.keyType)
                && Objects.equals(this.valueType, other.valueType);
    }
}

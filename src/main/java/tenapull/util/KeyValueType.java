package tenapull.util;

import java.util.*;

/**
 * Represents a pair of Types, representing a key and a value
 *
 * @param <K> the key class
 * @param <V> the value calss
 */
public class KeyValueType<K, V> {
    private final Type<K> keyType;
    private final Type<V> valueType;

    /**
     * Instantiates a new Key value type.
     *
     * @param keyType   the key type
     * @param valueType the value type
     * @throws NullPointerException if one of the arguments is null
     */
    public KeyValueType(Type<K> keyType, Type<V> valueType)
            throws NullPointerException {
        
        if (keyType == null || valueType == null) {
            throw new NullPointerException();
        }

        this.keyType = keyType;
        this.valueType = valueType;
    }

    /**
     * Instantiates a new Key value type.
     *
     * @param keyType   the key class
     * @param valueType the value class
     * @throws NullPointerException if one of the arguments is null
     */
    public KeyValueType(Class<K> keyType, Class<V> valueType)
            throws NullPointerException {

        this.keyType = new Type(keyType);
        this.valueType = new Type(valueType);
    }

    /**
     * Instantiates a new Key value type.
     *
     * @param keyType   the key class
     * @param valueType the value type
     * @throws NullPointerException if one of the arguments is null
     */
    public KeyValueType(Class<K> keyType, Type<V> valueType)
            throws NullPointerException {

        if (valueType == null) {
            throw new NullPointerException();
        }

        this.keyType = new Type(keyType);
        this.valueType = valueType;
    }

    /**
     * Instantiates a new Key value type.
     *
     * @param keyType   the key type
     * @param valueType the value class
     * @throws NullPointerException if one of the arguments is null
     */
    public KeyValueType(Type<K> keyType, Class<V> valueType)
            throws NullPointerException {

        if (keyType == null) {
            throw new NullPointerException();
        }

        this.keyType = keyType;
        this.valueType = new Type(valueType);
    }

    /**
     * Gets key type.
     *
     * @return the key type
     */
    public Type<K> getKeyType() {
        return this.keyType;
    }

    /**
     * Gets value type.
     *
     * @return the value type
     */
    public Type<V> getValueType() {
        return this.valueType;
    }

    public String toString() {
        return "(KeyValueType)<" + this.keyType.toStringBasic() + ", " + this.valueType.toStringBasic() + ">";
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

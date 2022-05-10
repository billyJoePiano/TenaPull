package nessusTools.data.entity.template;

import com.fasterxml.jackson.core.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Interface for "complex lookup" (or "object lookup") pojos, which use a hash of their
 * serialized JSON for indexing and ensuring uniqueness.  Most implementations
 * of this interface will inherit from the abstract HashLookupTemplate, but a
 * handful do not include ExtraJson so implement this interface on their own.
 *
 * @param <POJO> The POJO class implementing HashLookupPojo
 */
@MappedSuperclass
public interface HashLookupPojo<POJO extends HashLookupPojo<POJO>>
        extends DbPojo, Comparable<POJO> {

    /**
     * Get the _hash of the "object lookup" pojo used for indexing it.
     * @return the Hash of the object lookup
     */
    public Hash get_hash();

    /**
     * Set the _hash of the pojo used for indexing it
     * @param hash the hash to set
     * @throws IllegalStateException if the hash is being changed to a value different
     * than previously set.  All values including the hash are intended to be immutable
     * once a lookup record is created
     */
    public void set_hash(Hash hash) throws IllegalStateException;

    /**
     * Shortcut for the StringHashLookupDao to determine what approach to most efficiently use
     * to find a matching value for this lookup.  If the hash is already calculated, it is
     * most efficient to use the hash.  If it is not, it may be more efficient to use the _match method
     *
     * @return true if the hash has already been calculated, false if not
     */
    public boolean _isHashCalculated();

    /**
     * Determine if this pojo represents the same DB record as another POJO of the same type.
     *
     * Note that this is NOT the same as the equals() method (though it may be in certain cases).
     * Equals may be used to determine if two pojos of the same type contain all of the same values,
     * regardless of whether they represent the same DB record (depending on implementation)
     * while _match determines only if they represent the same DB record even if some of the values may
     * not be equivalent.  In many cases the two methods may be the same, but their purpose is different.
     *
     *
     * @param other other pojo to match
     * @return true if the two pojos represent the same DB record, false if not
     */
    public boolean _match(POJO other);

    /**
     * Synchronize the values of two matching POJOs which represent the same DB record
     *
     * @param other the other pojo representing the same DB record, which the values
     *              should be taken from
     */
    public void _set(POJO other);

    /**
     * Default implementation of the Comparable interface.
     * Compares two HashLookupPojos based on a comparison of their hash
     *
     * @param other the other HashLookupPojo of the same type
     * @return the result of the hash comparison
     */
    default public int compareTo(POJO other) {
        if (other == null) return -1;
        Hash mine = this.get_hash();
        Hash theirs = other.get_hash();

        if (mine == null) return 1;

        return mine.compareTo(theirs);
    }
}

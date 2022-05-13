package tenapull.data.entity.template;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.*;

/**
 * A "complex lookup" or "object lookup" altnerative to the HashLookupPojo interface
 * Rather than using a hash of the object, this approach using a search map instead.
 *
 * This pojo type is often (though not always) used for records that are NOT immutable
 * but have a composite candidate key (e.g. ScanHost, with a host_id and scan_id). It may
 * also be used for immutable object lookups in place of a hash lookup when the pojo has
 * a very simple object structure, so the overhead of calculating a hash (which also means
 * serializing the object to obtain the string to be hashed... arguably the more expensive
 * operation) may not be worth the indexing benefit it provides
 *
 * @param <POJO> the POJO class implementing MapLookupPojo
 */
@MappedSuperclass
public interface MapLookupPojo<POJO extends MapLookupPojo> extends DbPojo {
    /**
     * Set the values in this POJO to be identical to another POJO representing the same DB record
     *
     * @param other
     */
    @Transient
    @JsonIgnore
    public void _set(POJO other);

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
     * @param other the other pojo to match
     * @return true if the two pojos represent the same DB record, false if not
     */
    @Transient
    @JsonIgnore
    public boolean _match(POJO other);

    /**
     * Return a map to be used by the POJO type's dao, to find the database record matching the
     * same record that this pojo represents
     *
     * @return
     */
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap();
}

package nessusTools.data.entity.template;

import com.fasterxml.jackson.core.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@MappedSuperclass
public interface HashLookupPojo<POJO extends HashLookupPojo<POJO>>
        extends DbPojo, Comparable<POJO> {

    public Hash get_hash();
    public void set_hash(Hash hash) throws IllegalStateException;

    public boolean _isHashCalculated();

    public boolean _match(POJO other);
    public void _set(POJO other);

    default public int compareTo(POJO other) {
        if (other == null) return -1;
        Hash mine = this.get_hash();
        Hash theirs = other.get_hash();

        if (mine == null) return 1;

        return mine.compareTo(theirs);
    }
}

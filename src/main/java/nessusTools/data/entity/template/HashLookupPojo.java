package nessusTools.data.entity.template;

import javax.persistence.*;

@MappedSuperclass
public interface HashLookupPojo<POJO extends HashLookupPojo<POJO>>
        extends DbPojo, Comparable<POJO> {

    public byte[] get_hash();
    public void set_hash(byte[] hash) throws IllegalStateException;

    public boolean _isHashCalculated();

    public boolean _match(POJO other);
    public void _set(POJO other);

    default public int compareTo(POJO other) {
        if (other == null) return -1;
        byte[] mine = this.get_hash();
        byte[] theirs = other.get_hash();

        for (int i = 0; i < mine.length && i < theirs.length; i++) {
            if (mine[i] == theirs[i]) continue;
            return mine[i] < theirs[i] ? -1 : 1;
        }
        return 0;
    }
}

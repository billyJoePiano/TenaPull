package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import nessusTools.util.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import java.util.*;

@MappedSuperclass
public abstract class HashLookupTemplate<POJO extends HashLookupTemplate<POJO>>
        extends GeneratedIdPojo implements HashLookupPojo<POJO> {

    @NaturalId
    @Access(AccessType.PROPERTY)
    @Column(name = "_hash")
    @JsonIgnore
    private byte[] _hash;

    @JsonIgnore
    @Override
    public byte[] get_hash() {
        if (this._hash == null) {
            this._hash = Hash.Sha512(this.toString());
        }
        return this._hash;
    }

    @JsonIgnore
    @Override
    public void set_hash(byte[] hash) throws IllegalStateException {
        if (this._hash != null && !Hash.equals(this._hash, hash)) {
            throw new IllegalStateException("Cannot alter the hash of a HashLookup (" +
                    this.getClass() +") after it has been set!");
        }
        this._hash = hash;
    }

    @Transient
    @Override
    protected void __set(GeneratedIdPojo other) {
        super.__set(other);
        this.set_hash(((HashLookupTemplate)other)._hash);
    }

    @Transient
    @JsonIgnore
    public boolean _isHashCalculated() {
        return this._hash != null;
    }
}

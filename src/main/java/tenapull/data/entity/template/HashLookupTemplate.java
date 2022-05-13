package tenapull.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import java.util.*;

/**
 * The standard implementation of the HashLookupPojo interface.  This is used
 * by the majority of HashLookupPojos, although a handful do not use it because
 * it inherits extra json functionality from ExtensibleJsonPojo
 *
 * @param <POJO> the POJO type implementing HashLookupTemplate and HashLookupPojo
 */
@MappedSuperclass
public abstract class HashLookupTemplate<POJO extends HashLookupTemplate<POJO>>
        extends GeneratedIdPojo implements HashLookupPojo<POJO> {

    @NaturalId
    @Access(AccessType.PROPERTY)
    @Column(name = "_hash")
    @JsonIgnore
    @Convert(converter = Hash.Converter.class)
    private Hash _hash;

    @JsonIgnore
    @Override
    public Hash get_hash() {
        if (this._hash == null) {
            this._hash = new Hash(this.toJsonNode().toString());
        }
        return this._hash;
    }

    @JsonIgnore
    @Override
    public void set_hash(Hash hash) throws IllegalStateException {
        if (this._hash != null && !Objects.equals(this._hash, hash)) {
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

    public void __prepare() {
        super.__prepare();
        if (this._hash == null) this.get_hash();
    }

    public int hashCode() {
        return this.get_hash().hashCode();
    }
}

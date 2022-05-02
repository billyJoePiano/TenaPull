package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.persistence.*;
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
    @Convert(converter = Hash.Converter.class)
    private Hash _hash;

    @JsonIgnore
    @Override
    public Hash get_hash() {
        if (this._hash == null) {
            this._hash = new Hash(this.toString());
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


    private static Map<Class<? extends HashLookupTemplate>, Map<Hash, CacheMaps>>
            nodes = new LinkedHashMap<>();

    private static class CacheMaps {

    }

    private static class NodeCache {
        private final Hash hash;
        private final ObjectNode node;
        private final String string;
        private NodeCache(ObjectNode node) {
            this.node = node;
            this.string = node.toString();
            this.hash = new Hash(this.string);
        }
    }


    public ObjectNode toJsonNode() {
        return super.toJsonNode();
    }

}

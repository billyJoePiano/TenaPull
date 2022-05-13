package tenapull.data.entity.template;

import com.fasterxml.jackson.annotation.*;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.*;

/**
 * Represents POJOs with extensible JSON and a natural primary key id provided by the Nessus API
 */
@MappedSuperclass
public abstract class NaturalIdPojo extends ExtensibleJsonPojo
        implements DbPojo {
    @Id
    @NaturalId
    @JsonProperty
    private int id;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(o.getClass(), this.getClass())) return false;

        NaturalIdPojo other = (NaturalIdPojo) o;
        return  this.getId() == other.getId()
                && Objects.equals(this.toJsonNode(), other.toJsonNode());

    }

    /**
     * Convenience method for any subclasses which wish to implement one of
     * the lookup interfaces (MapLookup or HashLookup).  This can be invoked
     * to synchronize the ExtraJson between two pojos representing the same DB record.
     * Unlike the NaturalIdPojo, __set here does NOT set the id, because it is a surrogate
     * key and not a natural part of the Nessus API response
     *
     * @param o the other pojo which also represents the same DB record, to synchronize
     *          this pojo's extraJson with
     */
    @Transient
    @JsonIgnore
    protected void __set(NaturalIdPojo o) {
        this.setId(o.getId());
        this.setExtraJson(o.getExtraJson());
    }

    /**
     * Default implementation of the java hashCode() method used by HashMap and HashSet.
     * Performs a bitwise XOR on the hashCode of the implementing type's .class object,
     * and the id of the pojo.
     *
     * @return a java hash code to uniquely identify and match this pojo with other pojos
     * representing the same DB record
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode() ^ this.getId();
    }
}

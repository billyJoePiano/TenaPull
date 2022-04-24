package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.*;

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
}

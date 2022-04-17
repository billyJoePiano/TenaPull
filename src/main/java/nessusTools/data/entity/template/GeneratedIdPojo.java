package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.util.*;


@MappedSuperclass
public abstract class GeneratedIdPojo extends ExtensibleJsonPojo {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    @JsonIgnore
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

        ExtensibleJsonPojo other = (ExtensibleJsonPojo) o;
        return  (this.getId() == 0 || other.getId() == 0 || this.getId() == other.getId())
                && Objects.equals(this.toJsonNode(), other.toJsonNode());

    }
}
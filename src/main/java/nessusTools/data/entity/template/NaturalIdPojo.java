package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@MappedSuperclass
public abstract class NaturalIdPojo extends ExtensibleJsonPojo implements DbPojo {
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
}

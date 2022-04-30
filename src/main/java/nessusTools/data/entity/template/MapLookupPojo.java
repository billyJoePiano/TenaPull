package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.*;

@MappedSuperclass
public interface MapLookupPojo<OL extends MapLookupPojo> extends DbPojo {
    @Transient
    @JsonIgnore
    public void _set(OL other);

    @Transient
    @JsonIgnore
    public boolean _match(OL other);

    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap();
}

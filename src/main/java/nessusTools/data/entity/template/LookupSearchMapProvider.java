package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.*;

public interface LookupSearchMapProvider<POJO extends LookupSearchMapProvider<POJO>>
        extends ObjectLookupPojo<POJO> {

    public boolean _lookupMatch(POJO other);

    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap();
}

package nessusTools.data.entity.template;

import javax.persistence.*;

@MappedSuperclass
public interface ObjectLookupPojo<OL extends ObjectLookupPojo> extends DbPojo {
    public void _set(OL other);
}

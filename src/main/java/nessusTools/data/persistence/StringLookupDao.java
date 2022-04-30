package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;

public interface StringLookupDao<POJO extends StringLookupPojo> {
    public POJO getOrCreate(String string);
}

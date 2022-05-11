package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;

/**
 * The interface for String lookup daos, implemented by both SimpleStringLookupDao
 * and StringHashLookupDao (which inherits from HashLookupDao).
 *
 * @param <POJO> the type parameter
 */
public interface StringLookupDao<POJO extends StringLookupPojo> {
    /**
     * Gets or creates a string lookup pojo representing the provided string
     *
     * @param string the string
     * @return the or create
     */
    public POJO getOrCreate(String string);
}

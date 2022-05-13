package tenapull.data.persistence;

import tenapull.data.entity.template.*;
import tenapull.data.entity.template.DbPojo;
import tenapull.sync.*;


/**
 * Used for simple string lookups, typically of DB data type varchar(255)
 * though may be shorter than 255 in some cases.  These string lookups do
 * NOT use a hash value for indexing
 *
 * @param <POJO> the pojo type
 */
public class SimpleStringLookupDao<POJO extends SimpleStringLookupPojo<POJO>>
        extends Dao<POJO> implements StringLookupDao<POJO> {

    private InstancesTracker<String, POJO> workingInstances;

    /**
     * Instantiates a new Simple string lookup dao using the provided pojo type
     *
     * @param pojoType the pojo type
     * @throws IllegalArgumentException if a dao has already been instantiated
     * for the provided pojoType
     */
    public SimpleStringLookupDao(Class<POJO> pojoType) throws IllegalArgumentException {
        super(pojoType);
        this.workingInstances = new InstancesTracker<String, POJO>(String.class, pojoType, str -> {
            if (str == null) return null;
            this.holdSession();
            SessionTracker session = null;
            try {
                session = this.getSession();

                POJO pojo = (POJO)session.session.bySimpleNaturalId(this.getPojoType()).load(str);

                if (pojo != null) return pojo;

                try {
                    pojo = this.getPojoType().getDeclaredConstructor().newInstance();

                } catch (Exception e) {
                    throw new LookupException(e, this.getPojoType());
                }

                pojo.setValue(str);
                this.saveOrUpdate(pojo);

                return pojo;

            } finally {
                if (session != null) {
                    session.done(this);
                }
                this.releaseSession();
            }
        });
    }

    public POJO getOrCreate(String string) {
        if (string == null) return null;
        return this.workingInstances.getOrConstruct(string);
    }



    public String toString() {
        return "[SimpleStringLookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    /**
     * Gets the representing the provided SimpleStringLookupPojo type
     *
     * @param <P>            the type parameter
     * @param <D>            the type parameter
     * @param lookupPojoType the lookup pojo type
     * @return the d
     */
    public static <P extends DbPojo, D extends Dao<P>> D
            get(Class<P> lookupPojoType) {

        D dao = Dao.get(lookupPojoType);
        if (dao != null && dao instanceof SimpleStringLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
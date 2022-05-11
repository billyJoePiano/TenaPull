package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;


/**
 * Dao used for string lookups of the longtext db data type,
 * which cannot be indexed by MySQL.  Therefore a hash of the string
 * is used for indexing instead
 *
 * @param <POJO> the type parameter
 */
public class StringHashLookupDao<POJO extends StringHashLookupPojo<POJO>>
        extends HashLookupDao<POJO> implements StringLookupDao<POJO> {

    /**
     * Instantiates a new String hash lookup dao.
     *
     * @param pojoType the pojo type
     * @throws IllegalArgumentException if a dao has already been instantiated
     * for the provided pojoType
     */
    public StringHashLookupDao(Class<POJO> pojoType) throws IllegalArgumentException {
        super(pojoType);
    }

    public POJO getOrCreate(String string) {
        if (string == null) return null;

        Hash hash = new Hash(string);
        return this.instancesByHash.getOrConstructWith(hash, h -> {
            if (h == null) return null;
            this.holdSession();
            SessionTracker session = null;
            try {
                session = getSession();

                POJO pojo = session.session.bySimpleNaturalId(this.getPojoType()).load(hash);

                if (pojo != null) {
                    return pojo;
                }

                try {
                    pojo = this.getPojoType().getDeclaredConstructor().newInstance();

                } catch (Exception e) {
                    throw new LookupException(e, this.getPojoType());
                }

                pojo.setValue(string);
                pojo.set_hash(h);

                int i = this.insert(pojo);
                if (i != -1) {
                    POJO p = pojo;
                    return this.instances.constructWith(i, ii -> p);

                } else {
                    return pojo;
                }

            } finally {
                if (session != null) {
                    session.done(this);
                }

                this.releaseSession();
            }
        });
    }



    public String toString() {
        return "[StringHashLookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    /**
     * Get the StringHashLookupDao which represents the provided StringHashLookUpPojo type
     *
     * @param <P>            the type parameter
     * @param <D>            the type parameter
     * @param lookupPojoType the lookup pojo type
     * @return the d
     */
    public static <P extends DbPojo, D extends Dao<P>> D
            get(Class<P> lookupPojoType) {

        D dao = Dao.get(lookupPojoType);
        if (dao != null && dao instanceof StringHashLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
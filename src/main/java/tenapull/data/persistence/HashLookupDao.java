package tenapull.data.persistence;

import tenapull.data.entity.template.*;
import tenapull.sync.*;
import org.hibernate.*;
import org.hibernate.proxy.*;

import java.util.*;


/**
 * Dao used for object lookups that are indexed by their hash.  In addition to the instances
 * tracker (organized by primary key id) inherited from AbstractPojoLookupDao, this also includes
 * a second instances tracker organized by
 *
 * @param <POJO> the type parameter
 */
public class HashLookupDao<POJO extends HashLookupPojo<POJO>>
        extends AbstractPojoLookupDao<POJO> {

    /**
     * The "canonical" pojo instances organized by hash.
     */
    protected InstancesTracker<Hash, POJO> instancesByHash;

    /**
     * Instantiates a new Hash lookup dao for the provided pojo type
     *
     * @param pojoType the pojo type
     * @throws IllegalArgumentException if a dao has already been instantiated
     * for the provided pojoType
     */
    public HashLookupDao(Class<POJO> pojoType) throws IllegalArgumentException {
        super(pojoType);
        this.instancesByHash = new InstancesTracker<Hash, POJO>(Hash.class, pojoType, null);
    }

    /**
     * Gets the canonical instance representing the provided hash, if one exists
     *
     * @param hash the hash
     * @return the by hash
     */
    public POJO getByHash(Hash hash) {
        if (hash == null) return null;
        return this.instancesByHash.getOrConstructWith(hash, h -> {
            SessionTracker session = getSession();
            try {
                return session.session.bySimpleNaturalId(this.getPojoType()).load(h);

            } finally {
                session.done(this);
            }
        });
    }

    protected POJO checkedGetOrCreate(POJO pojo) {
        int id = pojo.getId();

        if (!pojo._isHashCalculated()) {
            POJO result = tryMatchFilter(pojo);
            if (result != null) {
                if (result != pojo && result._isHashCalculated()) {
                    pojo.set_hash(result.get_hash());
                }
                return finalizeResult(pojo, result);
            }
        }

        return finalizeResult(pojo, useHash(pojo));
    }

    protected POJO searchForInstanceId(POJO pojo, int id) {
        if ((pojo instanceof HibernateProxy && !Hibernate.isInitialized(pojo))
                || !pojo._isHashCalculated()) {

            return this.instances.get(id);

        } else {
            return this.instancesByHash.getOrConstructWith(pojo.get_hash(),
                    hash -> super.searchForInstanceId(pojo, id));
        }
    }

    /**
     * Attempts to use the _match function to find a matching pojo, so that
     * calculating the hash can be skipped when possible
     *
     * @param pojo the pojo
     * @return the pojo
     */
    protected POJO tryMatchFilter(POJO pojo) {
        List<POJO> list = this.instancesByHash.get(
                other -> other == pojo || (other != null && pojo._match(other)), 1);

        for (POJO other : list) {
            if (other != null) {
                return other;
            }
        }

        return null;
    }

    /**
     * Obtains the hash of the pojo and uses it to find a matching pojo if one exists,
     * otherwise attempts to find it in the database.  If none can be found, the new record
     * is inserted to the DB and the passed pojo is used as the new canonical instance for
     * this record
     *
     * @param pojo the pojo
     * @return the pojo
     */
    protected POJO useHash(POJO pojo) {
        return this.instancesByHash.getOrConstructWith(pojo.get_hash(), hash -> {
            if (hash == null) return null;
            this.holdSession();
            SessionTracker session = null;
            try {
                session = getSession();

                POJO r = session.session.bySimpleNaturalId(this.getPojoType()).load(hash);

                if (r != null) {
                    return r;
                }

                int i = this.insert(pojo, false);
                if (i != -1) {
                    return this.instances.constructWith(i, ii -> pojo);

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

    protected POJO finalizeResult(POJO pojo, POJO result) {
        if (result == null) {
            result = pojo;

        } else if (result != pojo) {
            result._set(pojo);
        }
        result._prepare();
        return result;
    }



    public String toString() {
        return "[HashLookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    /**
     * Get HashLookupDao for the provided pojo type.
     *
     * @param <P>            the type parameter
     * @param <D>            the type parameter
     * @param lookupPojoType the lookup pojo type
     * @return the d
     */
    public static <P extends DbPojo, D extends Dao<P>> D
            get(Class<P> lookupPojoType) {

        D dao = Dao.get(lookupPojoType);
        if (dao != null && dao instanceof HashLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
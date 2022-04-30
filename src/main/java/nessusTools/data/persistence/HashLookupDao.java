package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.sync.*;
import org.hibernate.*;
import org.hibernate.proxy.*;

import java.util.*;


public class HashLookupDao<POJO extends HashLookupPojo<POJO>>
        extends AbstractPojoLookupDao<POJO> {

    protected InstancesTracker<Hash, POJO> instancesByHash;

    public HashLookupDao(Class<POJO> pojoType) {
        super(pojoType);
        this.instancesByHash = new InstancesTracker<Hash, POJO>(Hash.class, pojoType, null);
    }

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
            if (result != null) return finalizeResult(pojo, result);
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

                int i = this.insert(pojo);
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
package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.util.*;


public class StringHashLookupDao<POJO extends StringHashLookupPojo<POJO>>
        extends HashLookupDao<POJO> implements StringLookupDao<POJO> {

    public StringHashLookupDao(Class<POJO> pojoType) {
        super(pojoType);
    }

    public POJO getOrCreate(String string) {
        if (string == null) return null;

        byte[] hash = Hash.Sha512(string);
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
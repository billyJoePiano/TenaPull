package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;
import org.hibernate.*;


public class LookupDao<POJO extends LookupPojo> extends ObjectLookupDao<POJO> {

    public LookupDao(Class<POJO> pojoClass) {
        super(pojoClass);
    }

    public POJO getOrCreate(String string) throws LookupException {
        if (string == null) {
            return null;
        }

        Session session = sessionFactory.openSession();
        POJO obj = session.byNaturalId(this.getPojoClass())
                .using(LookupPojo.FIELD_NAME, string).load();
        // https://stackoverflow.com/questions/14977018/jpa-how-to-get-entity-based-on-field-value-other-than-id

        if (obj != null) {
            return obj;
        }

        POJO pojo = null;

        try {
            pojo = this.getPojoClass().getDeclaredConstructor().newInstance();

        } catch(Exception e) {
            throw new LookupException(e, this.getPojoClass());
        }

        if (pojo == null) {
            throw new LookupException("Null pojo returned for '" + string + "'",
                    this.getPojoClass());
        }

        pojo.setValue(string);

        if (this.insert(pojo) != -1) {
            return pojo;

        } else {
            throw new LookupException("Couldn't create pojo '" + string + "'",
                    this.getPojoClass());
        }
    }

    public String toString() {
        return "[LookupDao for " + this.getPojoClass().getSimpleName() + "]";
    }

    public static <P extends DbPojo, D extends Dao<P>> D
            get(Class<P> lookupPojoClass) {

        D dao = Dao.get(lookupPojoClass);
        if (dao != null && dao instanceof LookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
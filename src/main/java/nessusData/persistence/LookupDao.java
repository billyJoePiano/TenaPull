package nessusData.persistence;

import nessusData.entity.template.LookupPojo;
import nessusData.entity.template.Pojo;
import org.hibernate.*;


public class LookupDao<POJO extends LookupPojo> extends Dao<POJO> {

    public LookupDao(Class<POJO> pojoClass) {
        super(pojoClass);
    }

    public POJO getOrCreate(String string) throws LookupException {
        if (string == null) {
            return null;
        }

        Session session = sessionFactory.openSession();
        Object obj = session.byNaturalId(this.getPojoClass())
                .using(LookupPojo.FIELD_NAME, string).load();
        // https://stackoverflow.com/questions/14977018/jpa-how-to-get-entity-based-on-field-value-other-than-id

        if (obj != null) {
            return (POJO) obj;
        }

        POJO pojo;

        try {
            pojo = (POJO) this.getPojoClass().getDeclaredConstructor().newInstance();

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
        return "[LookupDao for POJO class " + this.getPojoClass().toString() + "]";
    }

    public static LookupDao get(Class<? extends Pojo> lookupPojoClass) {
        Dao dao = Dao.get(lookupPojoClass);
        if (dao != null && dao instanceof LookupDao) {
            return (LookupDao) dao;

        } else {
            return null;
        }
    }
}
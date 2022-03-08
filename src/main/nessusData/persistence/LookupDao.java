package main.nessusData.persistence;

import main.nessusData.entity.LookupPojo;

import org.hibernate.*;


public class LookupDao<POJO extends LookupPojo> extends Dao<POJO> {
    private final String fieldName;

    public LookupDao(Class<POJO> pojoClass, final String fieldName) {
        super(pojoClass);
        this.fieldName = fieldName;
    }

    public POJO getOrCreate(String string) throws Exception {
        if (string == null) {
            return null;
        }

        Session session = sessionFactory.openSession();
        Object obj = session.byNaturalId(this.getPojoClass())
                .using(this.fieldName, string).load();
        // https://stackoverflow.com/questions/14977018/jpa-how-to-get-entity-based-on-field-value-other-than-id

        if (obj != null) {
            return (POJO) obj;
        }

        POJO pojo = (POJO) this.getPojoClass().newInstance();
        pojo.setString(string);
        if (this.insert(pojo) != -1) {
            return pojo;

        } else {
            throw new Exception("Couldn't create pojo for class "
                    + this.getPojoClass().toString());
        }
    }

    public String toString() {
        return "[LookupDao for POJO class " + this.getPojoClass().toString() + "]";
    }
}
package main.nessusData.persistence;

import main.nessusData.entity.LookupPojo;

import org.hibernate.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class LookupDao<POJO extends LookupPojo> extends Dao<POJO> {
    private final String fieldName;

    public LookupDao(Class pojoClass, final String fieldName) {
        super(pojoClass);
        this.fieldName = fieldName;
    }

    public POJO getOrCreate(String string) throws Exception {
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
}
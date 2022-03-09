package nessusData.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jdk.dynalink.linker.support.Lookup;
import nessusData.entity.*;

import org.hibernate.*;

import java.io.IOException;
import java.sql.Timestamp;


public class LookupDao<POJO extends LookupPojo> extends Dao<POJO> {
    private final String fieldName;

    public LookupDao(Class<POJO> pojoClass, final String fieldName) {
        super(pojoClass);
        this.fieldName = fieldName;
    }

    public POJO getOrCreate(String string) throws LookupException {
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

        pojo.setString(string);
        if (this.insert(pojo) != -1) {
            return pojo;

        } else {
            throw new LookupException("Couldn't create pojo '" + string + "'",
                    this.getPojoClass());
        }
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String toString() {
        return "[LookupDao for POJO class " + this.getPojoClass().toString() + "]";
    }

    public static LookupDao get(Class lookupPojoClass) {
        Dao dao = Dao.get(lookupPojoClass);
        if (dao != null && dao instanceof LookupDao) {
            return (LookupDao) dao;

        } else {
            return null;
        }
    }
}
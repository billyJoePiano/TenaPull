package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;
import nessusTools.sync.*;
import org.hibernate.*;

import java.util.*;


public class LookupDao<POJO extends LookupPojo<POJO>> extends Dao<POJO> {

    private InstancesTracker<String, POJO> workingInstances;

    public LookupDao(Class<POJO> pojoType) {
        super(pojoType);
        this.workingInstances = new InstancesTracker<String, POJO>(String.class, pojoType, str -> {
            if (str == null) return null;
            List<POJO> list = this.findByPropertyEqual("value", str);

            for (POJO p : list) {
                if (p != null && Objects.equals(p.getValue(), str)) return p;
            }

            POJO pojo;
            try {
                pojo = this.getPojoType().getDeclaredConstructor().newInstance();

            } catch (Exception e) {
                throw new LookupException(e, this.getPojoType());
            }

            pojo.setValue(str);
            this.saveOrUpdate(pojo);

            return pojo;
        });
    }

    public POJO getOrCreate(String string) {
        if (string == null) return null;
        return this.workingInstances.getOrConstruct(string);
    }



    public String toString() {
        return "[LookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    public static <P extends DbPojo, D extends Dao<P>> D
            get(Class<P> lookupPojoType) {

        D dao = Dao.get(lookupPojoType);
        if (dao != null && dao instanceof LookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
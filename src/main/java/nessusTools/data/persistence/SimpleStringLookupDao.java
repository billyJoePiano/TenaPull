package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;
import nessusTools.sync.*;


public class SimpleStringLookupDao<POJO extends SimpleStringLookupPojo<POJO>>
        extends Dao<POJO> implements StringLookupDao<POJO> {

    private InstancesTracker<String, POJO> workingInstances;

    public SimpleStringLookupDao(Class<POJO> pojoType) {
        super(pojoType);
        this.workingInstances = new InstancesTracker<String, POJO>(String.class, pojoType, str -> {
            if (str == null) return null;
            this.holdSession();
            SessionTracker session = null;
            try {
                session = this.getSession();

                POJO pojo = (POJO)session.session.bySimpleNaturalId(this.getPojoType()).load(str);

                if (pojo != null) return pojo;

                try {
                    pojo = this.getPojoType().getDeclaredConstructor().newInstance();

                } catch (Exception e) {
                    throw new LookupException(e, this.getPojoType());
                }

                pojo.setValue(str);
                this.saveOrUpdate(pojo);

                return pojo;

            } finally {
                if (session != null) {
                    session.done(this);
                }
                this.releaseSession();
            }
        });
    }

    public POJO getOrCreate(String string) {
        if (string == null) return null;
        return this.workingInstances.getOrConstruct(string);
    }



    public String toString() {
        return "[SimpleStringLookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    public static <P extends DbPojo, D extends Dao<P>> D
            get(Class<P> lookupPojoType) {

        D dao = Dao.get(lookupPojoType);
        if (dao != null && dao instanceof SimpleStringLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
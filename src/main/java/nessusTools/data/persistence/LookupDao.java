package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;
import org.hibernate.*;


public class LookupDao<POJO extends LookupPojo<POJO>> extends ObjectLookupDao<POJO> {

    public LookupDao(Class<POJO> pojoType) {
        super(pojoType);
    }

    public POJO getOrCreate(String string) {
        POJO pojo;
        try {
            pojo = this.getPojoType().getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new LookupException(e, this.getPojoType());
        }
        pojo.setValue(string);
        return this.getOrCreate(pojo);
    }

    @Override
    protected POJO useSearchMapProvider(POJO mapProvider) throws LookupException {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;

        try {
            return session.byNaturalId(this.getPojoType())
                    .using(LookupPojo.FIELD_NAME, mapProvider.getValue()).load();
            // https://stackoverflow.com/questions/14977018/jpa-how-to-get-entity-based-on-field-value-other-than-id

        } finally {
            sessionTracker.done();
        }
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
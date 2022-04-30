package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;

public interface StringLookupDao<POJO extends StringLookupPojo> {
    public POJO getOrCreate(String string);

    public static class Get {
        private Get() { }

        static <P extends StringLookupPojo, D extends Dao<P> & StringLookupDao<P>>
        D get(Class<P> lookupPojoType) {

            Dao dao = Dao.get(lookupPojoType);
            if (dao != null && dao instanceof StringLookupDao) {
                return (D) dao;

            } else {
                return null;
            }
        }
    }
}

package nessusData.persistence;

import nessusData.entity.*;

import org.hibernate.*;

import java.util.*;


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

        if (runningTests) {
            persistLater(pojo);
            return pojo;
        }

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

    /**
     * This is needed when running certain kinds of unit tests where
     * JSON is deserialized into POJOs before DB reset(s) happen
     */
    private static boolean runningTests = false;
    public static void runningTests() {
        runningTests = true;
    }

    private static List<Runnable> persistLater = null;

    public static Runnable getPersistLater() {
        Runnable sleep = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        if (persistLater == null) return sleep;

        List<Runnable> group = persistLater;
        persistLater = null;

        return () -> {
            for (Runnable persistLookup : group) {
                runningTests = false;
                persistLookup.run();
                runningTests = true;
                sleep.run();
            }
            for (int i = 0; i < 8; i++) {
                sleep.run();
            }
        };
    }

    private void persistLater(POJO pojo) {
        if (persistLater == null) {
            persistLater = new ArrayList();
        }
        persistLater.add(() -> {
            POJO persisted;
            try {
                persisted = this.getOrCreate(pojo.toString());

            } catch (LookupException e) {
                logger.error(e);
                return;
            }

            pojo.setId(persisted.getId());

        });
    }
}
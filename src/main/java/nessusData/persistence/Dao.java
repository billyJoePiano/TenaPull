package nessusData.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import nessusData.entity.*;
import nessusData.entity.*;

import org.apache.logging.log4j.*;
import org.hibernate.*;

import javax.persistence.criteria.*;
import java.util.*;


public class Dao<POJO extends Pojo> {
    private static Map<Class, Dao> classMap = new HashMap<Class, Dao>();

    public static final SessionFactory sessionFactory = SessionFactoryProvider.getSessionFactory();
    public static Dao get(Class<? extends Pojo> pojoClass) {
        return classMap.get(pojoClass);
    }

    protected final Logger logger;
    private final Class pojoClass;

    public Dao(final Class<POJO> pojoClass) {
        if (classMap.containsKey(pojoClass)) {
            throw new IllegalArgumentException("A Dao for this class already exists: "
                    + pojoClass.toString());
        }

        this.pojoClass = pojoClass;
        classMap.put(pojoClass, this);
        logger = LogManager.getLogger(pojoClass);
    }

    public Class<POJO> getPojoClass() {
        return this.pojoClass;
    }

    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Get POJO by id
     */
    public POJO getById(int id) {
        Session session = sessionFactory.openSession();
        POJO pojo = (POJO) session.get(this.getPojoClass(), id );
        session.close();
        return pojo;
    }

    /**
     * update POJO
     * @param pojo  POJO to be inserted or updated
     */
    public void saveOrUpdate(POJO pojo) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(pojo);
        transaction.commit();
        session.close();
    }

    /**
     * insert POJO
     * @param pojo  POJO to be inserted
     */
    public int insert(POJO pojo) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Integer id = null;

        try {
            tx = session.beginTransaction();
            id = (Integer) session.save(pojo);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();

            String json;

            try {
                json = pojo.toJson();

            } catch (JsonProcessingException ex) {
                json = "<ERROR PROCESSING JSON>";
            }

            logger.error("Error inserting record:\n" + json, e);

        } finally {
            session.close();
        }

        if (id == null) return -1;
        else return id;
    }


    /**
     * Delete a POJO
     * @param pojo pojo to be deleted
     */
    public void delete(POJO pojo) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(pojo);
        transaction.commit();
        session.close();
    }


    /** Return a list of all POJOs
     *
     * @return All POJOs
     */
    public List<POJO> getAll() {

        Session session = sessionFactory.openSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<POJO> query = builder.createQuery(this.getPojoClass());
        Root<POJO> root = query.from(this.getPojoClass());
        List<POJO> pojos = session.createQuery(query).getResultList();

        logger.debug("The list of POJOs " + pojos);
        session.close();

        return pojos;
    }

    /**
     * Finds entities by one of its properties.
     * @param propertyName the property name.
     * @param value the value by which to find.
     * @return
     */
    public List<POJO> findByPropertyEqual(String propertyName, Object value) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<POJO> query = builder.createQuery(pojoClass);
        Root<POJO> root = query.from(pojoClass);
        query.select(root).where(builder.equal(root.get(propertyName),value));

        return session.createQuery(query).getResultList();
    }

    /**
     * Finds entities by multiple properties.
     * Inspired by https://stackoverflow.com/questions/11138118/really-dynamic-jpa-criteriabuilder
     * @param propertyMap property and value pairs
     * @return entities with properties equal to those passed in the map
     *
     *
     */
    public List<POJO> findByPropertyEqual(Map<String, Object> propertyMap) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<POJO> query = builder.createQuery(pojoClass);
        Root<POJO> root = query.from(pojoClass);
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (Map.Entry entry: propertyMap.entrySet()) {
            predicates.add(builder.equal(root.get((String) entry.getKey()), entry.getValue()));
        }
        query.select(root).where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

        return session.createQuery(query).getResultList();
    }

    /**
     * Returns an open session from the SessionFactory
     * @return session
     */
    private Session getSession() {
        return sessionFactory.openSession();
    }

    public String toString() {
        return "[Dao for POJO class " + this.getPojoClass().toString() + "]";
    }

}

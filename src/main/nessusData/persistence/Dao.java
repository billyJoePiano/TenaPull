package main.nessusData.persistence;

import main.nessusData.entity.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;


public class Dao<POJO> {
    public static final SessionFactory sessionFactory = SessionFactoryProvider.getSessionFactory();

    private static Map<Class, Dao> classMap = new TreeMap<Class, Dao>();
    public static Dao get(Class pojoClass) {
        return classMap.get(pojoClass);
    }

    protected final Logger logger;
    private final Class pojoClass;

    public Dao(final Class pojoClass) {
        if (classMap.containsKey(pojoClass)) {
            throw new IllegalArgumentException("A Dao for this class already exists: "
                    + pojoClass.toString());
        }

        this.pojoClass = pojoClass;
        classMap.set(pojoClass, this);
        logger = LogManager.getLogger(pojoClass);
    }

    public Class getPojoClass() {
        return this.pojoClass;
    }

    /**
     * Get POJO by id
     */
    public POJO getById(int id) {
        Session session = sessionFactory.openSession();
        POJO pojo = session.get(POJO.class, id );
        session.close();
        return pojo;
    }

    /**
     * update POJO
     * @param POJO  POJO to be inserted or updated
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
     * @param POJO  POJO to be inserted
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
            logger.error("Error inserting author", e);

        } finally {
            session.close();
        }

        if (id == null) return -1;
        else return id;
    }


    /**
     * Delete a POJO
     * @param POJO pojo to be deleted
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
        CriteriaQuery<POJO> query = builder.createQuery(POJO.class);
        Root<POJO> root = query.from(POJO.class);
        List<POJO> pojos = session.createQuery(query).getResultList();

        logger.debug("The list of POJOs " + pojos);
        session.close();

        return pojos;
    }


    public static void main (String[] args) {
        Scan test = new Scan();
    }
}

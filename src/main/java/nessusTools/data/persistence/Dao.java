package nessusTools.data.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;

import nessusTools.data.entity.template.DbPojo;
import net.bytebuddy.implementation.bytecode.*;
import org.apache.logging.log4j.*;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.SingleTableEntityPersister;


import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.*;
import java.util.*;


public class Dao<POJO extends DbPojo> {
    private static final Logger staticLogger = LogManager.getLogger(Dao.class);
    private static final Map<Class<DbPojo>, Dao<DbPojo>> classMap = new HashMap();
    private static final SessionFactoryBuilder sessionFactoryBuilder = makeSessionFactoryBuilder();
    private static final SessionFactory sessionFactory = makeSessionFactory();
    private static final Map<Thread, SessionTracker> sessions = new WeakHashMap<>();

    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> pojoType) {
        return (D) classMap.get(pojoType);
    }

    private static SessionFactoryBuilder makeSessionFactoryBuilder() {
        try {
            return (new MetadataSources(new StandardServiceRegistryBuilder().configure().build()))
                    .getMetadataBuilder().build().getSessionFactoryBuilder();

        } catch (Throwable e) {
            staticLogger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private static SessionFactory makeSessionFactory() {
        try {
            return sessionFactoryBuilder.build();

        } catch (Throwable e) {
            staticLogger.error(e);
            throw new IllegalStateException(e);
        }
    }

    protected final Logger logger;
    private final Class<POJO> pojoType;

    private Map<String, Attribute<? super POJO, ?>> attributeMap = null;
    private Map<String, PropertyAccess> accessMap = null;
    private Map<String, Getter> getterMap = null;
    private Map<String, Attribute<? super POJO, ?>> idMap = null;

    public Dao(final Class<POJO> pojoType) {
        if (classMap.containsKey(pojoType)) {
            throw new IllegalArgumentException("A Dao for this class already exists: "
                    + pojoType.toString());
        }

        this.pojoType = pojoType;
        classMap.put((Class<DbPojo>) pojoType, (Dao<DbPojo>) this);
        logger = LogManager.getLogger(pojoType);
    }

    public Class<POJO> getPojoType() {
        return this.pojoType;
    }

    public Logger getLogger() {
        return this.logger;
    }

    protected static SessionTracker getSession() {
        synchronized (sessions) {
            SessionTracker tracker = sessions.get(Thread.currentThread());
            if (tracker == null) {
                tracker = new SessionTracker();
            }
            tracker.count++;
            return tracker;
        }
    }

    protected static class SessionTracker {
        protected final Session session = sessionFactory.openSession();
        protected final Thread thread = Thread.currentThread();
        private Transaction transaction;
        protected int count = 0;

        private SessionTracker() {
            synchronized (sessions) {
                sessions.put(thread, this);
            }
        }

        protected void done() {
            this.count--;
            if (count <= 0) {
                synchronized (sessions) {
                    sessions.remove(this.thread);
                }

                if (this.transaction != null) {
                    try {
                        Transaction transaction = this.transaction;
                        this.transaction = null;
                        transaction.commit();

                    } catch(Throwable e) {
                        staticLogger.error(e);
                    }
                }
                session.close();
            }
        }

        protected Transaction getTransaction() {
            if (this.transaction == null) {
                this.transaction = session.beginTransaction();
            }
            return this.transaction;
        }

        protected void failed(Throwable e, Class<? extends DbPojo> pojoType) throws LookupException {
            if (this.count > 1) {
                throw new LookupException(e, pojoType);

            } else if (this.transaction != null) {
                try {
                    Transaction transaction = this.transaction;
                    this.transaction = null;
                    transaction.rollback();

                } catch (Throwable e2) {
                    staticLogger.error(e2);
                }
            }
        }
    }



    /**
     * Get POJO by id
     */
    public POJO getById(int id) {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;

        try {
            POJO pojo = session.get(this.getPojoType(), id);
            return pojo;

        } catch (Throwable e) {
            logger.error("Error getting record by id", e);
            sessionTracker.failed(e, this.pojoType);
            return null;

        } finally {
            sessionTracker.done();
        }
    }

    /**
     * update POJO
     *
     * @param pojo POJO to be inserted or updated
     */
    public void saveOrUpdate(POJO pojo) {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;
        Transaction tx = null;

        try {
            tx = sessionTracker.getTransaction();
            session.saveOrUpdate(pojo);

        } catch (Throwable e) {
            String json;

            try {
                json = pojo.toJsonString();

            } catch (JsonProcessingException ex) {
                json = "<ERROR PROCESSING JSON : " + pojo.toString() + " >";
            }

            logger.error("Error save/updating record:\n" + json, e);

            sessionTracker.failed(e, this.pojoType);

        } finally {
            sessionTracker.done();
        }
    }

    /**
     * insert POJO
     *
     * @param pojo POJO to be inserted
     */
    public int insert(POJO pojo) {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;
        Transaction tx = null;
        Integer id = null;

        try {
            tx = sessionTracker.getTransaction();
            id = (Integer) session.save(pojo);

        } catch (Throwable e) {
            String json;

            try {
                json = pojo.toJsonString();

            } catch (JsonProcessingException ex) {
                json = "<ERROR PROCESSING JSON : " + pojo.toString() + " >";
            }

            logger.error("Error inserting record:\n" + json, e);

            sessionTracker.failed(e, this.pojoType);

        } finally {
            sessionTracker.done();
        }

        if (id == null) return -1;
        else return id;
    }


    /**
     * Delete a POJO
     *
     * @param pojo pojo to be deleted
     */
    public void delete(POJO pojo) {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;

        try {
            Transaction transaction = sessionTracker.getTransaction();
            session.delete(pojo);

        } catch (Throwable e) {
            String json;

            try {
                json = pojo.toJsonString();

            } catch (JsonProcessingException ex) {
                json = "<ERROR PROCESSING JSON : " + pojo.toString() + " >";
            }

            logger.error("Error deleting record:\n" + json, e);

            sessionTracker.failed(e, this.pojoType);

        } finally {
            sessionTracker.done();
        }
    }


    /**
     * Return a list of all POJOs
     *
     * @return All POJOs
     */
    public List<POJO> getAll() {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<POJO> query = builder.createQuery(this.getPojoType());
            Root<POJO> root = query.from(this.getPojoType());
            return session.createQuery(query).getResultList();

        } catch (Throwable e) {
            logger.error("Error getting all records", e);
            sessionTracker.failed(e, this.pojoType);

            return Collections.emptyList();

        } finally {
            sessionTracker.done();
        }
    }

    /**
     * Finds entities by one of its properties.
     *
     * @param propertyName the property name.
     * @param value        the value by which to find.
     * @return
     */
    public List<POJO> findByPropertyEqual(String propertyName, Object value) {
        return keyValueSearch(propertyName, value);
    }

    /**
     * Finds entities by multiple properties.
     * Inspired by https://stackoverflow.com/questions/11138118/really-dynamic-jpa-criteriabuilder
     *
     * @param propertyMap property and value pairs
     * @return entities with properties equal to those passed in the map
     */
    public List<POJO> findByPropertyEqual(Map<String, Object> propertyMap) {
        return mapSearch(propertyMap);
    }

    protected List<POJO> keyValueSearch(String propertyName, Object value) {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<POJO> query = builder.createQuery(pojoType);
            Root<POJO> root = query.from(pojoType);

            if (value != null) {
                query.select(root).where(builder.equal(root.get(propertyName), value));

            } else {
                query.select(root).where(builder.isNull(root.get(propertyName)));
            }

            return session.createQuery(query).getResultList();

        } catch(EntityNotFoundException e) {
            return Collections.emptyList();

        }  finally {
            sessionTracker.done();
        }
    }

    // this method is kept seperate so that the methods which need to call it can be overridden
    // in ObjectLookupDao without overriding this functionality

    protected List<POJO> mapSearch(Map<String, Object> propertyMap) {
        SessionTracker sessionTracker = getSession();
        Session session = sessionTracker.session;

        List<Predicate> predicates = new ArrayList<Predicate>();
        CriteriaBuilder builder;
        CriteriaQuery<POJO> query;

        try {
            builder = session.getCriteriaBuilder();
            query = builder.createQuery(pojoType);

            Root<POJO> root = query.from(pojoType);


            for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
                String propertyName = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    predicates.add(builder.isNull(root.get(propertyName)));

                } else {
                    predicates.add(builder.equal(root.get(propertyName), value));

                }
            }
            query.select(root).where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

            return session.createQuery(query).getResultList();

        } catch (Throwable e) {
            logger.error(e);
            return Collections.emptyList();

        } finally {
            sessionTracker.done();
        }
    }

    // Search on all fields EXCEPT id.  Return exact match only
    // IMPORTANT: the id of the passed searchPojo will be mutated in order to test equality.
    // IF no match is found, its id will be restored to the original value.  If a match is found
    // the id of the match will be left in the searchPojo.
    public POJO findByExactPojo(POJO searchPojo) {
        if (searchPojo == null) {
            return null;
        }

        int origId = searchPojo.getId();
        Map<String, Object> searchMap = makeExactPojoSearchMap(searchPojo);

        for (POJO pojo : this.mapSearch(searchMap)) {
            searchPojo.setId(pojo.getId());
            if (searchPojo.equals(pojo)) {
                return pojo;
            }
        }

        searchPojo.setId(origId);
        return null;
    }

    protected Map<String, Object> makeExactPojoSearchMap(POJO searchPojo) {
        Map<String, Object> searchMap = new HashMap();
        Map<String, Attribute<? super POJO, ?>> attributeMap = this.getAttributeMap();

        for (Map.Entry<String, Getter> entry
                : this.getGetterMap().entrySet()) {

            String field = entry.getKey();
            Attribute attribute = attributeMap.get(field);
            if (attribute != null && attribute.isCollection()) {
                continue;
            }

            Object value = entry.getValue().get(searchPojo);

            searchMap.put(field, value);
        }
        return searchMap;
    }

    // Search on all non-null fields except id.  Return a list of matching entries
    public List<POJO> findByPojoNonNull(POJO searchPojo) {
        if (searchPojo == null) {
            return null;
        }
        Map<String, Object> searchMap = makePojoNonNullSearchMap(searchPojo);
        return this.mapSearch(searchMap);
    }

    protected Map<String, Object> makePojoNonNullSearchMap(POJO searchPojo) {
        Map<String, Object> searchMap = new HashMap();

        for (Map.Entry<String, Getter> entry
                : this.getGetterMap().entrySet()) {

            Object value = entry.getValue().get(searchPojo);
            if (value != null) {
                searchMap.put(entry.getKey(), value);
            }
        }
        return searchMap;
    }


    public Map<String, Attribute<? super POJO, ?>> getAttributeMap() {
        if (this.attributeMap == null) {
            makeAccessorMaps();
        }
        return this.attributeMap;
    }

    public Map<String, PropertyAccess> getAccessMap() {
        if (this.accessMap == null) {
            makeAccessorMaps();
        }
        return this.accessMap;
    }

    public Map<String, Getter> getGetterMap() {
        if (this.getterMap == null) {
            makeAccessorMaps();
        }
        return this.getterMap;
    }

    /*
    public Map<String, Class<AttributeConverter>> getConverterMap() {
        if (this.converterMap == null) {
            makeAccessorMaps();
        }
        return this.converterMap;
    }
     */

    public Map<String, Attribute<? super POJO, ?>> getIdMap() {
        if (this.idMap == null) {
            makeAccessorMaps();
        }
        return this.idMap;
    }

    private void makeAccessorMaps() {
        MetamodelImplementor metamodel = (MetamodelImplementor)  sessionFactory.getMetamodel();
        EntityTypeDescriptor<POJO> entity = metamodel.entity(this.getPojoType());
        SingleTableEntityPersister metadata = (SingleTableEntityPersister) metamodel.entityPersister(this.getPojoType());

        Map<String, Attribute<? super POJO, ?>> attributeMap = new HashMap();
        Map<String, PropertyAccess> accessMap = new HashMap();
        Map<String, Getter> getterMap = new HashMap();
        Map<String, Class<AttributeConverter>> converterMap = new HashMap();

        Map<String, Attribute<? super POJO, ?>> idMap = new HashMap();

        for (Attribute<? super POJO, ?> attribute : entity.getAttributes()) {
            String fieldname = attribute.getName();


            if (attribute instanceof SingularAttribute) {
                if (((SingularAttribute) attribute).isId()) {
                    idMap.put(fieldname, attribute);
                    continue;
                }
            }

            PropertyAccess accessor =
                    PropertyAccessStrategyFieldImpl.INSTANCE.buildPropertyAccess(this.getPojoType(), fieldname);

            attributeMap.put(fieldname, attribute);
            accessMap.put(fieldname, accessor);
            getterMap.put(fieldname, accessor.getGetter());
        }

        this.attributeMap = Collections.unmodifiableMap(attributeMap);
        this.accessMap = Collections.unmodifiableMap(accessMap);
        this.getterMap = Collections.unmodifiableMap(getterMap);
        this.idMap = Collections.unmodifiableMap(idMap);
    }

    public String toString() {
        return "[Dao for " + this.getPojoType().getSimpleName() + "]";
    }

}

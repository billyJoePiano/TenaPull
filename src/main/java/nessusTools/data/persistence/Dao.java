package nessusTools.data.persistence;

import com.sun.istack.*;
import nessusTools.data.entity.template.DbPojo;
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
import org.hibernate.proxy.*;

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

    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> pojoType) {
        synchronized (Dao.class) {
            return (D) classMap.get(pojoType);
        }
    }

    private static SessionFactoryBuilder makeSessionFactoryBuilder() {
        try {
            return (new MetadataSources(new StandardServiceRegistryBuilder().configure().build()))
                    .getMetadataBuilder().build().getSessionFactoryBuilder();

        } catch (Exception e) {
            staticLogger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private static SessionFactory makeSessionFactory() {
        try {
            return sessionFactoryBuilder.build();

        } catch (Exception e) {
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

    private final Map<Thread, List<SessionTracker>> sessions = new WeakHashMap();
    private Set<Class<? extends DbPojo>> borrowSessionsFrom;
    private Map<Thread, SessionTracker> holdOpen = new WeakHashMap<>();

    private static Set<Class<? extends DbPojo>> notInitializedSessionLenders;


    public Dao(final Class<POJO> pojoType) {
        synchronized (Dao.class) {
            if (classMap.containsKey(pojoType)) {
                throw new IllegalArgumentException("A Dao for this class already exists: "
                        + pojoType.toString());
            }

            this.pojoType = pojoType;
            classMap.put((Class<DbPojo>) pojoType, (Dao<DbPojo>) this);
            logger = LogManager.getLogger(pojoType);
        }
    }

    public Class<POJO> getPojoType() {
        return this.pojoType;
    }

    public Logger getLogger() {
        return this.logger;
    }


    public void borrowSessionsFrom(Class<? extends DbPojo> pojoType) {
        synchronized (Dao.class) {
            if (this.borrowSessionsFrom == null) {
                this.borrowSessionsFrom = new LinkedHashSet<>();
            }
            this.borrowSessionsFrom.add(pojoType);
        }
    }

    public void holdSession() {
        Thread current = Thread.currentThread();
        synchronized (this.holdOpen) {
            this.holdOpen.put(current, getSession());
        }
    }

    public void releaseSession() {
        Thread current = Thread.currentThread();
        SessionTracker session;
        synchronized (this.holdOpen) {
            session = this.holdOpen.get(current);
            if (session == null) {
                throw new IllegalStateException("Cannot release session when none is being held open!");
            }
        }

        try {
            session.done(this);

        } finally {
            if (session.sharers == null || session.sharers.size() <= 0) {
                synchronized (this.holdOpen) {
                    this.holdOpen.remove(current);
                }
            }
        }
    }

    protected SessionTracker getSession() {

        SessionTracker session = null;
        Thread current = Thread.currentThread();
        synchronized (this.holdOpen) {
            session = this.holdOpen.get(current);
        }

        synchronized (Dao.class) {
            if (this.borrowSessionsFrom == null) {
                if (session == null) {
                    session = new SessionTracker();
                }

            } else if (session == null) {
                for (Class<? extends DbPojo> pojoType : this.borrowSessionsFrom) {
                    Dao dao = Dao.get(pojoType);
                    if (dao == null) continue;
                    synchronized (dao.sessions) {
                        List<SessionTracker> sessions
                                = (List<SessionTracker>) dao.sessions.get(current);

                        if (sessions == null || sessions.size() <= 0) continue;
                        session = sessions.get(sessions.size() - 1);
                        if (session != null) break;
                    }
                }
                if (session == null) {
                    session = new SessionTracker();
                }
            }
        }

        synchronized (this.sessions) {
            List<SessionTracker> sessions = this.sessions.get(current);
            if (sessions == null) {
                sessions = new ArrayList();
                this.sessions.put(current, sessions);
            }

            session.sharers.add(this);
            sessions.add(session);
        }

        return session;
    }

    public boolean hasActiveSession() {
        Thread current = Thread.currentThread();
        synchronized (this.sessions) {
            List<SessionTracker> sessions = this.sessions.get(current);

            if (sessions != null) {
                for (SessionTracker session : sessions) {
                    if (session != null) return true;
                }
            }
        }
        return false;
    }

    public boolean lenderHasActiveSession() {
        synchronized (Dao.class) {
            if (this.borrowSessionsFrom == null) return false;

            for (Class<? extends DbPojo> pojoType : this.borrowSessionsFrom) {
                Dao dao = Dao.get(pojoType);
                if (dao == null) continue;
                if (dao.hasActiveSession()) return true;
            }
        }
        return false;
    }

    protected static class SessionTracker {
        protected final Session session = sessionFactory.openSession();
        protected final Thread thread = Thread.currentThread();
        private Transaction transaction;
        protected List<Dao> sharers = new ArrayList();

        private SessionTracker() { }

        protected void done(Dao dao) {
            if (this.sharers == null) {
                this.close(dao);
                return;
            }

            int index = this.sharers.lastIndexOf(dao);

            if (index != -1) {
                this.sharers.remove(index);
            }
            if (this.sharers.size() <= 0) {
                this.close(dao);
                this.sharers = null;
                synchronized (dao.sessions) {
                    List<SessionTracker> sessions
                            = (List<SessionTracker>) dao.sessions.get(this.thread);

                    while (sessions.remove (this)) { }
                    if (sessions.size() <= 0) {
                        dao.sessions.remove(this.thread);
                    }
                }
            }
        }

        // only call from done() ... session users should only call done(this)
        private void close(Dao closer) {
            if (this.transaction != null) {
                Transaction transaction = this.transaction;
                this.transaction = null;
                try {
                    transaction.commit();

                } catch (Exception e) {
                    transaction.rollback();
                    closer.logger.error(e);
                }
            }

            try {
                session.close();

            } catch (Exception e) {
                closer.logger.error(e);
            }
        }


        protected Transaction getTransaction() {
            if (this.transaction == null) {
                this.transaction = session.beginTransaction();
            }
            return this.transaction;
        }

        protected void failed(Throwable e, Dao dao) throws LookupException {
            if (this.sharers != null && this.sharers.size() > 1) {
                throw new LookupException(e, dao.pojoType);
            }

            if (this.transaction != null) {
                try {
                    Transaction transaction = this.transaction;
                    this.transaction = null;
                    transaction.rollback();

                } catch (Exception e2) {
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

        } catch (Exception e) {
            logger.error("Error getting record by id", e);
            sessionTracker.failed(e, this);
            return null;

        } finally {
            sessionTracker.done(this);
        }
    }

    public void saveOrUpdate(POJO pojo) {
        this.saveOrUpdate(pojo, true);
    }

    /**
     * update POJO
     *
     * @param pojo POJO to be inserted or updated
     */
    protected void saveOrUpdate(POJO pojo, boolean runPrepare) {
        if (pojo == null) return;

        SessionTracker session = null;
        Transaction tx = null;

        try {
            if (runPrepare) {
                pojo._prepare();
            }

            session = getSession();
            tx = session.getTransaction();
            session.session.saveOrUpdate(pojo);

        } catch (Exception e) {
            logger.error("Error saving/updating record:\n" + pojo, e);

            if (session != null) {
                session.failed(e, this);
            }

        } finally {
            if (session != null) {
                session.done(this);
            }
        }
    }

    /**
     * insert POJO
     *
     * @param pojo POJO to be inserted
     */
    public int insert(POJO pojo) {
        return this.insert(pojo, true);
    }


    protected int insert(POJO pojo, boolean runPrepare) {
        if (pojo == null) return -1;

        SessionTracker session = null;
        Transaction tx = null;
        Integer id = null;

        try {
            if (runPrepare) {
                pojo._prepare();
            }

            session = getSession();
            tx = session.getTransaction();
            id = (Integer) session.session.save(pojo);

        } catch (Exception e) {
            logger.error("Error inserting record:\n" + pojo, e);

            if (session != null) {
                session.failed(e, this);
            }

        } finally {
            if (session != null) {
                session.done(this);
            }
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
        this.delete(pojo, true);
    }

    public void delete(POJO pojo, boolean runPrepare) {
        SessionTracker session = null;
        Transaction tx = null;

        try {
            if (runPrepare) {
                pojo._prepare();
            }

            session = getSession();
            tx = session.getTransaction();
            session.session.delete(pojo);

        } catch (Exception e) {
            logger.error("Error deleting record:\n" + pojo, e);

            if (session != null) {
                session.failed(e, this);
            }

        } finally {
            if (session != null) {
                session.done(this);
            }
        }
    }


    /**
     * Return a list of all POJOs
     *
     * @return All POJOs
     */
    public List<POJO> getAll() {
        SessionTracker session = getSession();

        try {
            CriteriaBuilder builder = session.session.getCriteriaBuilder();
            CriteriaQuery<POJO> query = builder.createQuery(this.getPojoType());
            Root<POJO> root = query.from(this.getPojoType());
            return session.session.createQuery(query).getResultList();

        } catch (Exception e) {
            logger.error("Error getting all records", e);
            session.failed(e, this);

            return Collections.emptyList();

        } finally {
            session.done(this);
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
        SessionTracker session = getSession();

        try {
            CriteriaBuilder builder = session.session.getCriteriaBuilder();
            CriteriaQuery<POJO> query = builder.createQuery(pojoType);
            Root<POJO> root = query.from(pojoType);

            if (value != null) {
                query.select(root).where(builder.equal(root.get(propertyName), value));

            } else {
                query.select(root).where(builder.isNull(root.get(propertyName)));
            }

            return session.session.createQuery(query).getResultList();

        } catch (Exception e) {
            logger.error("Error searching for property '" + propertyName
                            + "' with value '" + value + "'");

            session.failed(e, this);
            return Collections.emptyList();

        }  finally {
            session.done(this);
        }
    }

    // this method is kept seperate so that the methods which need to call it can be overridden
    // in MapLookupDao without overriding this functionality

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

        } catch (Exception e) {
            logger.error("Error searching based on key-value pairs");
            logger.error(e);
            return Collections.emptyList();

        } finally {
            sessionTracker.done(this);
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

    public POJO unproxy(POJO pojo) {
        if (pojo == null) return null;
        if (!(pojo instanceof HibernateProxy)) return pojo;
        HibernateProxy proxy = (HibernateProxy)pojo;

        if (!proxy.getHibernateLazyInitializer().isUninitialized()) {
            return (POJO)Hibernate.unproxy(pojo);
        }
        return checkedUnproxy(proxy);
    }

    protected POJO checkedUnproxy(@NotNull HibernateProxy pojo) {
        int id = ((POJO)pojo).getId();
        if (id > 0) {
            POJO result = null;
            this.holdSession();
            try {
                result = this.getById(id);
                if (result != null) {
                    Hibernate.initialize(result);
                }

            } catch (HibernateException e) {
                logger.warn("Hibernate proxy failed to initialize, in Dao.unproxy(pojo)");
                if (result != null) {
                    return result;

                } else {
                    return (POJO)pojo;
                }

            } finally {
                this.releaseSession();
            }

            if (result != null) return (POJO)Hibernate.unproxy(result);
        }

        try {
            this.holdSession();
            Hibernate.initialize(pojo);

        } catch (HibernateException e) {
            logger.warn("Hibernate Proxy wouldn't provide a valid id or "
                    + "allow initialization during MapLookupDao.getOrCreate(pojo)", e);

            return (POJO)pojo;

        } finally {
            this.releaseSession();
        }

        return (POJO)Hibernate.unproxy(pojo);
    }

}

package nessusData.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;

import nessusData.entity.template.Pojo;
import org.apache.logging.log4j.*;
import org.hibernate.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;


public class Dao<POJO extends Pojo> {
    private static Map<Class<Pojo>, Dao<Pojo>> classMap = new HashMap();

    public static final SessionFactory sessionFactory =
            (new MetadataSources(new StandardServiceRegistryBuilder().configure().build()))
                    .getMetadataBuilder().build().getSessionFactoryBuilder().build();

    public static <P extends Pojo, D extends Dao<P>> D get(Class<P> pojoClass) {
        return (D) classMap.get(pojoClass);
    }

    protected final Logger logger;
    private final Class<POJO> pojoClass;
    private List<String> fieldNames = null;

    private Map<String, Attribute<? super POJO, ?>> attributeMap = null;
    private Map<String, PropertyAccess> accessMap = null;
    private Map<String, Getter> getterMap = null;

    private Map<String, Attribute<? super POJO, ?>> idMap = null;

    public Dao(final Class<POJO> pojoClass) {
        if (classMap.containsKey(pojoClass)) {
            throw new IllegalArgumentException("A Dao for this class already exists: "
                    + pojoClass.toString());
        }

        this.pojoClass = pojoClass;
        classMap.put((Class<Pojo>) pojoClass, (Dao<Pojo>) this);
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
                json = pojo.toJsonString();

            } catch (JsonProcessingException ex) {
                json = "<ERROR PROCESSING JSON : " + pojo.toString() + " >";
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

        List<POJO> list = session.createQuery(query).getResultList();
        session.close();
        return list;
    }

    /**
     * Finds entities by multiple properties.
     * Inspired by https://stackoverflow.com/questions/11138118/really-dynamic-jpa-criteriabuilder
     * @param searchMap property and value pairs
     * @return entities with properties equal to those passed in the map
     *
     *
     */
    public List<POJO> findByPropertyEqual(Map<String, Object> searchMap) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<POJO> query = builder.createQuery(pojoClass);
        Root<POJO> root = query.from(pojoClass);
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (Map.Entry<String, Object> entry: searchMap.entrySet()) {
            predicates.add(builder.equal(root.get(entry.getKey()), entry.getValue()));
        }
        query.select(root).where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

        List<POJO> list = session.createQuery(query).getResultList();
        session.close();
        return list;
    }

    // Search on all fields EXCEPT id.  Return exact match only
    // IMPORTANT: the id of the passed searchPojo will be mutated in order to test equality.
    // IF no match is found, its id will be restored to the original value.  If a match is found
    // the id of the match will be left in the searchPojo.
    public POJO findByExactPojo(POJO searchPojo) {
        if (searchPojo == null) {
            return null;
        }
        Map<String, Object> searchMap = new HashMap();

        for (Map.Entry<String, Getter> entry
                : this.getGetterMap().entrySet()) {

            Object value = entry.getValue().get(searchPojo);
            searchMap.put(entry.getKey(), value);
        }

        int origId = searchPojo.getId();

        for (POJO pojo : this.findByPropertyEqual(searchMap)) {
            searchPojo.setId(pojo.getId());
            if (searchPojo.equals(pojo)) {
                return pojo;
            }
        }

        searchPojo.setId(origId);
        return null;
    }

    // Search on all non-null fields except id.  Return a list of matching entries
    public List<POJO> findByPojoNonNull(POJO searchPojo) {
        if (searchPojo == null) {
            return null;
        }
        Map<String, Object> searchMap = new HashMap();

        for (Map.Entry<String, Getter> entry
                : this.getGetterMap().entrySet()) {

            Object value = entry.getValue().get(searchPojo);
            if (value != null) {
                searchMap.put(entry.getKey(), value);
            }
        }

        return this.findByPropertyEqual(searchMap);
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

    private void makeAccessorMaps() {
        MetamodelImplementor metamodel = (MetamodelImplementor)  sessionFactory.getMetamodel();
        EntityTypeDescriptor<POJO> entity = metamodel.entity(this.getPojoClass());
        SingleTableEntityPersister metadata = (SingleTableEntityPersister) metamodel.entityPersister(this.getPojoClass());

        Map<String, Attribute<? super POJO, ?>> attributeMap = new HashMap();
        Map<String, PropertyAccess> accessMap = new HashMap();
        Map<String, Getter> getterMap = new HashMap();

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
                    PropertyAccessStrategyFieldImpl.INSTANCE.buildPropertyAccess(this.getPojoClass(), fieldname);

            attributeMap.put(fieldname, attribute);
            accessMap.put(fieldname, accessor);
            getterMap.put(fieldname, accessor.getGetter());
        }

        this.attributeMap = Collections.unmodifiableMap(attributeMap);
        this.accessMap = Collections.unmodifiableMap(accessMap);
        this.getterMap = Collections.unmodifiableMap(getterMap);
        this.idMap = Collections.unmodifiableMap(idMap);
    }

    /**
     * Returns an open session from the SessionFactory
     * @return session
     */
    private Session getSession() {
        return sessionFactory.openSession();
    }

    public String toString() {
        return "[Dao for " + this.getPojoClass().getSimpleName() + "]";
    }

}

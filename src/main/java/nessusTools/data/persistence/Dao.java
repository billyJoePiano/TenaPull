package nessusTools.data.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;

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

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.*;
import java.util.*;


public class Dao<POJO extends DbPojo> {
    private static final Logger staticLogger = LogManager.getLogger(Dao.class);
    private static final Map<Class<DbPojo>, Dao<DbPojo>> classMap = new HashMap();
    public static final SessionFactoryBuilder sessionFactoryBuilder = makeSessionFactoryBuilder();
    public static final SessionFactory sessionFactory = makeSessionFactory();

    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> pojoClass) {
        return (D) classMap.get(pojoClass);
    }

    private static SessionFactoryBuilder makeSessionFactoryBuilder() {
        try {
            return (new MetadataSources(new StandardServiceRegistryBuilder().configure().build()))
                    .getMetadataBuilder().build().getSessionFactoryBuilder();

        } catch(Throwable e) {
            staticLogger.error(e);
            return null;
        }
    }

    private static SessionFactory makeSessionFactory() {
        try {
            return sessionFactoryBuilder.build();

        } catch(Throwable e) {
            staticLogger.error(e);
            return null;
        }
    }

    protected final Logger logger;
    private final Class<POJO> pojoClass;

    private Map<String, Attribute<? super POJO, ?>> attributeMap = null;
    private Map<String, PropertyAccess> accessMap = null;
    private Map<String, Getter> getterMap = null;
    //private Map<String, Class<AttributeConverter>> converterMap = null;

    private Map<String, Attribute<? super POJO, ?>> idMap = null;

    public Dao(final Class<POJO> pojoClass) {
        if (classMap.containsKey(pojoClass)) {
            throw new IllegalArgumentException("A Dao for this class already exists: "
                    + pojoClass.toString());
        }

        this.pojoClass = pojoClass;
        classMap.put((Class<DbPojo>) pojoClass, (Dao<DbPojo>) this);
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
        POJO pojo = session.get(this.getPojoClass(), id );
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


        /*
        Class<AttributeConverter> converterClass = getConverterMap().get(propertyName);
        if (converterClass != null) {
            try {
                AttributeConverter converter
                        = converterClass.getDeclaredConstructor().newInstance();

                value = converter.convertToDatabaseColumn(value);

            } catch (Throwable e) {
                logger.error(e);
            }
        }
         */


        if (value != null) {
            query.select(root).where(builder.equal(root.get(propertyName), value));

        } else {
            query.select(root).where(builder.isNull(root.get(propertyName)));
        }

        List<POJO> list = session.createQuery(query).getResultList();
        session.close();
        return list;
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

        //Map<String, Class<AttributeConverter>> converterMap = getConverterMap();

        for (Map.Entry<String, Object> entry: propertyMap.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();

            /*
            //Do value conversion if necessary
            Class<AttributeConverter> converterClass = converterMap.get(propertyName);
            if (converterClass != null) {
                try {
                    AttributeConverter converter
                            = converterClass.getDeclaredConstructor().newInstance();

                    value = converter.convertToDatabaseColumn(value);

                } catch (Throwable e) {
                    logger.error(e);
                }
            }
            */

            if (value != null) {
                predicates.add(builder.equal(root.get(propertyName), value));

            } else {
                predicates.add(builder.isNull(root.get(propertyName)));
            }
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
        EntityTypeDescriptor<POJO> entity = metamodel.entity(this.getPojoClass());
        SingleTableEntityPersister metadata = (SingleTableEntityPersister) metamodel.entityPersister(this.getPojoClass());

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
                    PropertyAccessStrategyFieldImpl.INSTANCE.buildPropertyAccess(this.getPojoClass(), fieldname);

            attributeMap.put(fieldname, attribute);
            accessMap.put(fieldname, accessor);
            getterMap.put(fieldname, accessor.getGetter());

            /*
            // converter annotation
            // first ... walk up the inheritance hierarchy and try to find this property name
            Class cls = this.getPojoClass();
            Field field = null;
            NoSuchFieldException last = null;

            while (!cls.equals(DbPojo.class)) {
                try {
                    field = this.getPojoClass().getDeclaredField(fieldname);
                    break; // success!

                } catch (NoSuchFieldException e) {
                    if (last != null) {
                        e.initCause(last);
                    }
                    last = e;
                }
                cls = cls.getSuperclass();
            }

            Convert converter = null;
            if (field != null) {
                converter = field.getAnnotation(Convert.class);
            } else {
                logger.error(last);
            }

            if (converter != null) {
                converterMap.put(fieldname,
                        (Class<AttributeConverter>) converter.converter());
            }
             */
        }

        this.attributeMap = Collections.unmodifiableMap(attributeMap);
        this.accessMap = Collections.unmodifiableMap(accessMap);
        this.getterMap = Collections.unmodifiableMap(getterMap);
        //this.converterMap = Collections.unmodifiableMap(converterMap);
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

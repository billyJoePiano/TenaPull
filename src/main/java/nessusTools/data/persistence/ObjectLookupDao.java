package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;

import java.lang.ref.*;
import java.util.*;

import javax.json.JsonException;

import com.fasterxml.jackson.databind.*;
import nessusTools.sync.*;
import nessusTools.util.*;
import org.hibernate.*;
import org.hibernate.property.access.spi.*;


// TODO add code to update old PojoFinder keys for workingLookups when the pojo record is mutated

public class ObjectLookupDao<POJO extends ObjectLookupPojo<POJO>> extends Dao<POJO> {


    //whether to lookup the object by a (non-zero) id passed in the DbPojo
    private final boolean naturalId;
    private final boolean getByIdWhenZero;
    private final boolean searchMapProvider;
        // Typically, getByIdWhenZero will coincide with use of IdNullable deserializer

    public ObjectLookupDao(Class<POJO> pojoType) {
        this(pojoType, false);
    }

    public ObjectLookupDao(Class<POJO> pojoType, boolean getByIdWhenZero) {
        super(pojoType);
        this.naturalId = NaturalIdPojo.class.isAssignableFrom(pojoType);
        this.searchMapProvider = LookupSearchMapProvider.class.isAssignableFrom(pojoType);

        if (getByIdWhenZero && !this.naturalId) {
                throw new IllegalArgumentException("DbPojo class when getByIdWhenZero = true must be a NaturalIdPojo");
        }

        this.getByIdWhenZero = getByIdWhenZero;
        this.workingLookups = new InstancesTracker(new Type(PojoFinder.class, pojoType), new Type(pojoType), null);
    }


    private final InstancesTracker<PojoFinder, POJO> workingLookups;
    private final ReadWriteLock<Void, POJO> pojoFinderLock = new ReadWriteLock(null);

    public static final long TEMP_STRONG_REF_LIFETIME_MS = 10000;
    private static final ReadWriteLock<Map<ObjectLookupDao.PojoFinder, Void>,
            Map<ObjectLookupDao.PojoFinder, Void>>
            tempStrongRefs = ReadWriteLock.forMap(new WeakHashMap());

    private static final Var.Long gcMonitor = new Var.Long();


    private class PojoFinder implements InstancesTracker.KeyFinalizer<PojoFinder, POJO> {
        // Overrides the Object.equals method to match Pojos on different criteria, including ids,
        // custom equality lambda, or the POJOs own equals method

        private WeakReference<POJO> weakRef; // indicates this is a PojoFinder to be matched
        private POJO tempStrongRef;

        // indicates this is a PojoFinder LOOKING for a match
        private POJO searchingRef;
        private Integer id;

        private boolean multiSearch;


        private PojoFinder(int id) {
            this.id = id;
        }

        private PojoFinder(POJO pojo) {
            this.searchingRef = pojo;
            this.tempStrongRef = pojo;
        }

        private PojoFinder() {
            this.multiSearch = true;
        }

        private POJO get() {
            if (this.searchingRef != null) {
                return this.searchingRef;

            } else if (this.tempStrongRef != null) {
                return this.tempStrongRef;

            } else if (this.weakRef != null) {
                return this.weakRef.get();

            } else {
                return null;
            }
        }

        @Override
        public void finalizeKey(POJO instance) {
            if (this.multiSearch) {
                if (instance != null) {
                    throw new IllegalStateException("Multi-search POJO finder should never produce an instance");
                }

            } else {
                this.tempStrongRef = null;
                this.set(instance);
            }
        }

        private POJO set(POJO pojo, PojoFinder other) {
            return ObjectLookupDao.this.pojoFinderLock.write(na -> {
                this.setWithLock(pojo);
                if (other != this) {
                    other.setWithLock(pojo);
                }
                return pojo;
            });
        }

        private POJO set(POJO pojo) {
            return ObjectLookupDao.this.pojoFinderLock.write(na -> {
                this.setWithLock(pojo);
                return pojo;
            });
        }

        // ONLY CALL WITH WRITE LOCK ON ObjectLookupDao.this.pojoFinderLock
        private void setWithLock(POJO pojo) {
            if (this.multiSearch) {
                throw new IllegalStateException("Multi-search POJO finder should never have 'set()' called");
            }

            if (this.tempStrongRef != null && pojo != this.tempStrongRef) {
                this.tempStrongRef = pojo;
                this.weakRef = new WeakReference<>(pojo);

            } else if (this.weakRef == null || this.weakRef.get() != pojo) {
                this.weakRef = new WeakReference<>(pojo);
            }

            this.searchingRef = null;
            this.id = null;
        }

        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;

            if (!Objects.equals(o.getClass(), this.getClass())) {
                return false;
            }

            PojoFinder other = (PojoFinder) o;

            if (this.multiSearch) {
                return other.multiSearch;

            } else if (other.multiSearch) {
                return false;
            }


            return ObjectLookupDao.this.pojoFinderLock.read(Boolean.class,
                    na -> this.equalsWithLock(other));
        }

        // ONLY CALL WITH READ LOCK ON ObjectLookupDao.this.pojoFinderLock
        private boolean equalsWithLock(PojoFinder other) {
            POJO mine = this.get();
            POJO theirs = other.get();

            if (this.id != null) {
                if (other.id != null) {
                    return this.id.intValue() == other.id.intValue();

                } else if (theirs != null && (other.searchingRef == null || ObjectLookupDao.this.naturalId)) {
                    return theirs.getId() == this.id.intValue();
                }

            } else if (other.id != null) {
                if (mine != null && (other.searchingRef == null || ObjectLookupDao.this.naturalId)) {
                    return mine.getId() == other.id.intValue();
                }
            }

            if (ObjectLookupDao.this.searchMapProvider) {
                if (mine == null || theirs == null) {
                    return false;
                }
                LookupSearchMapProvider mn = (LookupSearchMapProvider) mine;
                LookupSearchMapProvider th = (LookupSearchMapProvider) theirs;
                return mn._lookupMatch(th);

            } else if (ObjectLookupDao.this.naturalId) {
                return mine != null && theirs != null
                        && mine.getId() == theirs.getId();

            } else if (mine != null && theirs != null) {
                if (mine.getId() == 0 || theirs.getId() == 0) {
                    // when id is a surrogate key and one of the objects' ids is unset...
                    // it means the objects should be compared with their own equals method
                    // which skips id comparison in this case, as defined by generatedIdPojo.equals
                    return Objects.equals(mine, theirs);

                } else {
                    return mine.getId() == theirs.getId();
                }

            } else {
                return false;
            }
        }
    }

    //NOTE for NaturalId pojos where the id is not zero OR getByIdWhenZero is true
    // this will UPDATE the existing record if the two POJOs are not equal
    public POJO getOrCreate(POJO pojo) throws LookupException {
        if (pojo == null) {
            return null;
        }

        if (!this.searchMapProvider) {
            if (pojo.getId() != 0) {
                return this.updateOrCreateById(pojo);

            } else if (this.naturalId) {
                if (this.getByIdWhenZero) {
                    return this.updateOrCreateById(pojo);
                }

                throw new LookupException("Invalid pojo submitted to objectLookupDao.getOrCreate() ... "
                        + "NaturalIdPojos must have a non-zero id unless getByIdWhenZero is true\n"
                        + pojo.toString(), this.getPojoType());

            }
        }

        PojoFinder finder = new PojoFinder(pojo);

        POJO val = this.workingLookups.getOrConstructWith(finder, f ->
            this.pojoFinderLock.write(this.getPojoType(), na -> {
                POJO result;
                if (this.searchMapProvider) {
                    result = ObjectLookupDao.this.useSearchMapProvider(pojo);

                } else {
                    result = super.findByExactPojo(pojo);
                }

                if (result != null) {
                    return f.set(result, finder);
                }

                if (super.insert(pojo) != -1) {
                    return f.set(pojo, finder);

                } else {
                    throw new LookupException("Couldn't create pojo '" + pojo + "'",
                            this.getPojoType());
                }
            })
        );


        if (val != pojo) {
            finder.set(val);
            if ((this.searchMapProvider || !this.naturalId)
                    && !Objects.equals(val, pojo)) {
                val._set(pojo);
            }
        }
        return val;
    }

    private synchronized POJO updateOrCreateById(POJO pojo) throws LookupException {
        PojoFinder multisearch = new PojoFinder(); //to lock the multi-search
        Var<POJO> result = new Var();

        this.workingLookups.constructWith(multisearch, msf ->
            this.pojoFinderLock.write(this.getPojoType(), na -> {
                PojoFinder finder = new PojoFinder(pojo);
                result.value = this.workingLookups.get(finder);

                if (result.value != null) {
                    if (!Objects.equals(result.value, pojo)) {
                        result.value._set(pojo);
                    }

                } else {
                    result.value = pojo;
                    this.workingLookups.put(finder, pojo);
                }

                finder.set(result.value);

                POJO dbPojo;
                if (this.searchMapProvider) {
                    dbPojo = this.useSearchMapProvider(result.value);

                } else {
                    dbPojo = super.getById(result.value.getId());
                }

                if (dbPojo != null) {
                    if (Objects.equals(dbPojo, result.value)) {
                        return null;
                    }

                    super.saveOrUpdate(result.value);

                    dbPojo = this.getById(pojo.getId());

                    if (!dbPojo.equals(result.value)) {
                        throw new LookupException("Unable to correctly assign object lookup DbPojo to values:\n"
                                + pojo.toString(), this.getPojoType());
                    }

                } else if (super.insert(result.value) == -1) {
                    throw new LookupException("Couldn't create pojo '" + pojo + "'",
                            this.getPojoType());
                }
                return null;
            })
        );

        return result.value;
    }


    private POJO useSearchMapProvider(POJO mapProvider) throws LookupException {
        LookupSearchMapProvider smp = (LookupSearchMapProvider) mapProvider;
        List<POJO> results = this.mapSearch(smp._getSearchMap());

        switch (results.size()) {
            case 0:
                return null;

            case 1:
                return results.get(0);

            default:
                throw new LookupException(
                        "LookupSearchMapProvider searchMap returned more than one instance ("
                        + results.size() + " returned)",
                        this.getPojoType());
        }
    }

    @Override
    public POJO getById(int id) {
        PojoFinder finder = new PojoFinder(id);
        return workingLookups.getOrConstructWith(finder, f -> {
            return f.set(super.getById(id), finder);
        });
    }

    @Override
    public void saveOrUpdate(POJO pojo) {
        if (this.getByIdWhenZero || pojo.getId() != 0) {
            this.updateOrCreateById(pojo);

        } else if (this.naturalId) {
            throw new LookupException(
                    "NaturalIdPojo 'ObjectLookup' Pojo MUST have a non-zero Id, unless its ObjectLookupDao has getByIdWhenZero = true\n"
                            + pojo.toString(),
                    this.getPojoType());

        } else {
            this.insert(pojo);
        }
    }

    @Override
    public synchronized int insert(POJO pojo) {
        if (this.naturalId && !this.getByIdWhenZero && pojo.getId() == 0) {
            throw new LookupException(
                    "NaturalIdPojo 'ObjectLookup' Pojo MUST have a non-zero Id, unless its ObjectLookupDao has getByIdWhenZero = true\n"
                        + pojo.toString(),
                    this.getPojoType());
        }

        PojoFinder finder = new PojoFinder(pojo);
        Var.Int id = new Var.Int();

        POJO val = this.workingLookups.constructWith(finder, f -> {
            POJO prev = this.workingLookups.get(f);

            if (prev == null) {
                if (this.searchMapProvider) {
                    prev = this.useSearchMapProvider(pojo);

                } else {
                    for (POJO other : this.mapSearch(
                                    this.makeExactPojoSearchMap(pojo))) {

                        if (pojo.equals(other)) {
                            prev = other;
                            break;
                        }
                    }
                }

                if (prev == null) {
                    id.value = super.insert(pojo);
                    return f.set(pojo, finder);
                }
            }

            logger.warn("Insert operation found another instance in ObjectLookupDao.insert(pojo).  "
                    + "This could cause Hibernate exceptions under certain circumstances."
                    + "\n      inserting: " + pojo
                    + "\nalready existed: " + prev
                    + "\n    new == old : " + (pojo == prev)
                    + "\nObjects.equals(new, old) : " + Objects.equals(pojo, prev));

            id.value = super.insert(pojo);
            return finder.set(pojo); //don't set 'f'
        });

        return id.value;
    }

    @Override
    public synchronized void delete(POJO pojo) {
        PojoFinder finder = new PojoFinder(pojo);
        Var<PojoFinder> other = new Var();
        this.workingLookups.constructWith(finder, f -> {
            this.pojoFinderLock.write(na -> {
                super.delete(pojo);

                // this may be overkill... 4 - 8 different calls to workingLookups.get/remove???
                List<POJO> instances = new ArrayList();

                Lambda1<PojoFinder, Void> doRemove = fndr -> {
                    POJO instance;

                    instance = this.workingLookups.get(fndr);
                    if (instance != null && !instances.contains(instance)) {
                        instances.add(instance);
                    }

                    instance = workingLookups.remove(fndr);
                    if (instance != null && !instances.contains(instance)) {
                        instances.add(instance);
                    }
                    return null;
                };

                doRemove.call(f);

                if (f == finder) {
                    f.set(null);
                    doRemove.call(f);

                } else if (f != finder) {
                    other.value = f;
                    doRemove.call(finder);

                    f.set(null, finder);

                    doRemove.call(f);
                    doRemove.call(finder);
                }

                boolean hadKeys;

                do {
                    hadKeys = false;

                    for (int i = 0; i < instances.size(); i++) {
                        POJO instance = instances.get(i);

                        Set<PojoFinder> keys = this.workingLookups.getKeysFor(instance);
                        if (keys.size() > 0) {
                            hadKeys = true;

                            for (PojoFinder key : keys) {
                                doRemove.call(key);
                                key.set(null);
                                doRemove.call(key);
                            }
                        }
                    }

                } while (hadKeys);

                return null;
            });
            return null;
        });

        this.workingLookups.remove(finder);
        if (other.value != null) {
            this.workingLookups.remove(other.value);
        }
    }

    public synchronized List<POJO> getAll() {
        this.workingLookups.constructWith(new PojoFinder(), f -> {
            for (POJO pojo : super.getAll()) {
                PojoFinder finder = new PojoFinder(pojo);

                POJO workingInstance = this.workingLookups.get(finder);

                if (workingInstance != null) {
                    if (workingInstance != pojo
                            && !Objects.equals(workingInstance, pojo)) {

                        workingInstance._set(pojo);
                    }

                    finder.set(workingInstance);

                } else {
                    this.workingLookups.put(finder, pojo);
                    finder.set(pojo);
                }

            }

            return null;
        });

        return new ArrayList(this.workingLookups.getInstances());
    }

    public List<POJO> findByPropertyEqual(String propertyName, Object value) {
        Getter getter = this.getGetterMap().get(propertyName);
        if (getter == null) {
            logger.error("Ignoring invalid propertyName submitted to ObjectLookupDao.findByPropertyEqual: "
                    + propertyName);
            return iterateGetterMap(new LinkedHashMap<>(), Map.of(propertyName, value), true);
        }
        return iterateGetterMap(Map.of(getter, value), Map.of(propertyName, value), true);
    }

    public List<POJO> findByPropertyEqual(Map<String, Object> searchMap) {
        Map<Getter, Object> getterMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
            String propertyName = entry.getKey();
            Getter getter = this.getGetterMap().get(propertyName);
            if (getter == null) {
                logger.error("Ignoring invalid propertyName submitted in searchMap to ObjectLookupDao.findByPropertyEqual: "
                        + propertyName);
                continue;
            }
            getterMap.put(getter, entry.getValue());
        }
        return iterateGetterMap(getterMap, searchMap, false);
    }


    private synchronized List<POJO> iterateGetterMap(Map<Getter, Object> getterMap,
                                        Map<String, Object> searchMap,
                                        boolean useSingleMethod) {

        Var.Bool reachedLambda = new Var.Bool(false);
        PojoFinder finder = new PojoFinder(); //multi-search finder
        Var<List<POJO>> newList = new Var();

        this.workingLookups.constructWith(finder, f -> {
            List<POJO> oldList = this.workingLookups.get(pojo -> {
                for (Map.Entry<Getter, Object> entry : getterMap.entrySet()) {
                    Getter getter = entry.getKey();
                    try {
                        if (!Objects.equals(entry.getValue(), getter.get(pojo))) {
                            return false;
                        }

                    } catch (HibernateException e) {
                        logger.warn("ObjectLookupDao.findByPropertyEqual caught "
                                + "a hibernate exception (lambda inside iterateGetterMap())");
                        logger.warn(e);
                        return false;
                    }
                }
                return true;
            });

            if (useSingleMethod) {
                if (searchMap.size() != 1) throw new IllegalStateException();
                for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
                    newList.value = this.keyValueSearch(entry.getKey(), entry.getValue());
                    break;
                }
            } else {
                newList.value = this.mapSearch(searchMap);
            }

            for (int i = 0; i < newList.value.size(); i++) {
                POJO newPojo = newList.value.get(i);

                int id = newPojo.getId();

                for (int j = 0; j < oldList.size(); j++) {
                    POJO oldPojo = oldList.get(j);

                    if (oldPojo.getId() != id) {
                        continue;
                    }

                    oldList.remove(j);

                    if (Objects.equals(oldPojo, newPojo)) {
                        newList.value.remove(i);
                        newList.value.add(i, oldPojo);

                    } else {
                        Set<PojoFinder> oldFinders = this.workingLookups.getKeysFor(oldPojo);
                        for (PojoFinder of : oldFinders) {
                            of.set(newPojo);
                            this.workingLookups.put(of, newPojo);
                        }
                    }
                    break;
                }
            }

            if (oldList.size() > 0) {
                logger.warn("ObjectLookupDao, Unexpected old Pojo(s) didn't match a new one:\n" + oldList);
            }

            return null;
        });

        return newList.value;
    }

    @Override
    public POJO findByExactPojo(POJO searchPojo) {
        PojoFinder finder = new PojoFinder(searchPojo);

        return this.workingLookups.getOrConstructWith(finder, f -> {
            POJO result;
            if (this.searchMapProvider) {
                result = this.useSearchMapProvider(searchPojo);

            } else {
                result = super.findByExactPojo(searchPojo);
            }

            if (result != null) {
                f.set(result, finder);
            }

            return result;
            // may return null if nothing was found
        });
    }




    public static Map<String, Object> makeSearchMapFromJson(JsonNode searchMapNode)
            throws JsonException {

        Map<String, Object> searchMap = new HashMap();

        Iterator<Map.Entry<String, JsonNode>> iterator = searchMapNode.fields();
        // I guess you can't do a for loop over an iterator, only an iterable!!
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String key = entry.getKey();
            JsonNode nodeVal = entry.getValue();

            if (!nodeVal.isValueNode()) {
                throw new JsonException("Invalid non-primitive value in searchMap."
                        + key + "\n" + searchMapNode.toString());
            }

            Object value;

            switch (nodeVal.getNodeType()) {
                case NUMBER:
                    if (nodeVal.isFloatingPointNumber()) {
                        value = nodeVal.doubleValue();

                    } else if (nodeVal.isInt()) {
                        value = nodeVal.intValue();

                    } else if (nodeVal.isLong()) {
                        value = nodeVal.longValue();

                    } else {
                        throw new JsonException("Could not determine numeric type conversion for searchMap."
                                + key + "\n" + searchMapNode.toString());
                    }
                    break;

                case BOOLEAN:
                    value = nodeVal.booleanValue();
                    break;

                case STRING:
                    value = nodeVal.textValue();
                    break;

                case NULL:
                    value = null;
                    break;

                default:
                    throw new JsonException("Could not determine value type for JsonNode in searchMap"
                            + key + "\n" + searchMapNode.toString());

            }

            searchMap.put(key, value);

        }

        return searchMap;
    }


    public String toString() {
        return "[ObjectLookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> objectLookupPojoClass) {
        D dao = Dao.get(objectLookupPojoClass);
        if (dao != null && dao instanceof ObjectLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
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
import org.hibernate.collection.internal.*;
import org.hibernate.property.access.spi.*;
import org.hibernate.proxy.*;


// TODO add code to update old PojoFinder keys for workingLookups when the pojo record is mutated

public class ObjectLookupDao<POJO extends ObjectLookupPojo<POJO>> extends Dao<POJO> {


    //whether to lookup the object by a (non-zero) id passed in the DbPojo
    private final boolean naturalId;
    private final boolean searchMapProvider;
        // Typically, getByIdWhenZero will coincide with use of IdNullable deserializer

    public ObjectLookupDao(Class<POJO> pojoType) {
        super(pojoType);
        this.naturalId = NaturalIdPojo.class.isAssignableFrom(pojoType);
        this.searchMapProvider = LookupSearchMapProvider.class.isAssignableFrom(pojoType);
        this.workingLookups = new InstancesTracker(Integer.class, pojoType, null);
    }


    private final InstancesTracker<Integer, POJO> workingLookups;

    public List<POJO> getOrCreate(List<POJO> list) {
        if (list == null) return null;

        List<POJO> newList = new ArrayList<>(list.size());
        for (POJO pojo : list) {
            newList.add(this.getOrCreate(pojo));
        }
        return newList;
    }

    public POJO getOrCreate(POJO pojo) {
        if (pojo == null) return null;
        pojo._prepare();
        if (pojo.getId() != 0) {
            POJO result = this.workingLookups.get(pojo.getId());
            if (result != null) {
                if (result != pojo) {
                    result._set(pojo);
                }
                result._prepare();
                return result;
            }
        }

        POJO result = null;
        synchronized (this) {
            List<POJO> list = this.workingLookups.get(other ->
                    other == pojo || (other != null && pojo._match(other)), 1);

            for (POJO p : list) {
                if (p != null) {
                    result = p;
                    break;
                }
            }

            if (result == null) {
                if (this.searchMapProvider) {
                    result = useSearchMapProvider(pojo);

                } else {
                    result = this.findByExactPojo(pojo);
                }

                if (result == null) {
                    int id = this.insert(pojo);
                    if (id != -1) {
                        result = this.workingLookups.constructWith(id, i -> pojo);
                    }
                    if (result == null) {
                        return pojo;
                    }
                }
            }
        }

        if (result != pojo) {
            result._set(pojo);
        }
        result._prepare();
        return result;
    }

    private synchronized POJO useSearchMapProvider(POJO mapProvider) throws LookupException {
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
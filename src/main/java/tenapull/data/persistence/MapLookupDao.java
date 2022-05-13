package tenapull.data.persistence;

import tenapull.data.entity.template.*;
import tenapull.data.entity.template.DbPojo;

import java.util.*;

import javax.json.JsonException;

import com.fasterxml.jackson.databind.*;
import org.hibernate.proxy.*;


/**
 * Dao used for map lookup pojos, where a DB record can be matched by a search map
 * or the _match function.  This pojo type is often (though not always) used for records with
 * a composite candidate key (e.g. ScanHost, with a host_id and scan_id) but may
 * also be used for immutable object lookups in place of a hash lookup for very simple
 * object structures where the overhead of a hash may not be worth the indexing benefit
 * it provides
 *
 * @param <POJO> the type parameter
 */
public class MapLookupDao<POJO extends MapLookupPojo<POJO>> extends AbstractPojoLookupDao<POJO> {

    /**
     * Instantiates a new MapLookupDao for the pojo type provided.
     *
     * @param pojoType
     * @throws IllegalArgumentException if a dao has already been instantiated
     * for the provided pojoType
     */
    public MapLookupDao(Class<POJO> pojoType) throws IllegalArgumentException {
        super(pojoType);
    }

    protected POJO checkedGetOrCreate(POJO pojo) {
        POJO result = null;

        synchronized (this) {
            List<POJO> list = this.instances.get(other ->
                    other == pojo || (other != null && pojo._match(other)), 1);

            for (POJO p : list) {
                if (p != null && p._match(pojo)) {
                    result = p;
                    break;
                }
            }

            if (result == null) {
                list = this.findByPropertyEqual(pojo._getSearchMap());
                for (POJO p : list) {
                    if (p != null && p._match(pojo)) {
                        result = this.instances.getOrConstructWith(p.getId(), i -> p);
                        break;
                    }
                }

                if (result == null) {
                    int id = this.insert(pojo);
                    if (id != -1) {
                        result = this.instances.constructWith(id, i -> pojo);
                    }
                }
            }
        }

        return finalizeResult(pojo, result);
    }

    @Override
    protected POJO finalizeResult(POJO pojo, POJO result) {
        if (result == null) {
            result = pojo;

        } else if (result != pojo) {
            result._set(pojo);
        }
        result._prepare();
        return result;
    }

    protected POJO checkedUnproxy(HibernateProxy pojo) {
        int id = ((POJO)pojo).getId();
        POJO result;
        if (id > 0) {
            result = this.instances.get(id);
            if (result != null && result != pojo) return result;
        }
        return super.checkedUnproxy(pojo);
    }

    private synchronized POJO useSearchMapProvider(POJO pojo) throws LookupException {
        List<POJO> results = this.mapSearch(pojo._getSearchMap());

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


    /**
     * Make a pojo search map from a json node
     *
     * @param searchMapNode the json node to make the search map for
     * @return the search map
     * @throws JsonException if there is an error processing json
     */
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
        return "[MapLookupDao for " + this.getPojoType().getSimpleName() + "]";
    }

    /**
     * Get the MapLookupDao for the provided pojo type
     *
     * @param <P>                   the type parameter
     * @param <D>                   the type parameter
     * @param objectLookupPojoClass the object lookup pojo class
     * @return the d
     */
    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> objectLookupPojoClass) {
        D dao = Dao.get(objectLookupPojoClass);
        if (dao != null && dao instanceof MapLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
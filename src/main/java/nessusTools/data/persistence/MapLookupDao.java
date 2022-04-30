package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;

import java.util.*;

import javax.json.JsonException;

import com.fasterxml.jackson.databind.*;
import org.hibernate.proxy.*;


// TODO add code to update old PojoFinder keys for workingLookups when the pojo record is mutated

public class MapLookupDao<POJO extends MapLookupPojo<POJO>> extends AbstractPojoLookupDao<POJO> {
    public MapLookupDao(Class<POJO> pojoType) {
        super(pojoType);
    }

    protected POJO checkedGetOrCreate(POJO pojo) {
        POJO result = null;

        synchronized (this) {
            List<POJO> list = this.instances.get(other ->
                    other == pojo || (other != null && pojo._match(other)), 1);

            for (POJO p : list) {
                if (p != null) {
                    result = p;
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

    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> objectLookupPojoClass) {
        D dao = Dao.get(objectLookupPojoClass);
        if (dao != null && dao instanceof MapLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
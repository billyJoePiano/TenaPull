package nessusData.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import nessusData.entity.template.*;
import org.hibernate.Session;
import java.util.*;

import javax.json.JsonException;

import com.fasterxml.jackson.databind.node.*;


public class ObjectLookupDao<POJO extends Pojo> extends Dao<POJO> {

    public ObjectLookupDao(Class<POJO> pojoClass) {
        super(pojoClass);
    }

    public POJO getOrCreate(ObjectNode object) throws LookupException {
        if (object == null) {
            return null;
        }

        Session session = sessionFactory.openSession();

        List<POJO> list = this.findByPropertyEqual(makeSearchMapFromJson(object));

        for (POJO pojo : list) {
            if (object.equals(pojo.toJsonNode())) {
                return pojo;
            }
        }

        POJO pojo;

        try {
            ObjectMapper mapper = new ObjectMapper();
            pojo = mapper.convertValue(object, this.getPojoClass());

        } catch(Exception e) {
            throw new LookupException(e, this.getPojoClass());
        }

        if (pojo == null) {
            throw new LookupException("Null pojo returned for '" + object + "'",
                    this.getPojoClass());
        }

        if (this.insert(pojo) != -1) {
            return pojo;

        } else {
            throw new LookupException("Couldn't create pojo '" + object + "'",
                    this.getPojoClass());
        }
    }

    public static Map<String, Object> makeSearchMapFromJson(JsonNode searchMapNode) {
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
        return "[ObjectLookupDao for " + this.getPojoClass().getSimpleName() + "]";
    }

    public static ObjectLookupDao get(Class<? extends Pojo> lookupPojoClass) {
        Dao dao = Dao.get(lookupPojoClass);
        if (dao != null && dao instanceof ObjectLookupDao) {
            return (ObjectLookupDao) dao;

        } else {
            return null;
        }
    }
}
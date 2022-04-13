package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.data.entity.template.DbPojo;

import java.util.*;

import javax.json.JsonException;

import com.fasterxml.jackson.databind.*;
import nessusTools.sync.*;


public class ObjectLookupDao<POJO extends DbPojo> extends Dao<POJO> {
    //whether to lookup the object by a (non-zero) id passed in the DbPojo
    private final boolean naturalId;
    private final boolean getByIdWhenZero;
        // Typically, getByIdWhenZero will coincide with use of IdNullable deserializer

    private final WeakInstancesTracker<POJO, POJO> workingLookups;

    public ObjectLookupDao(Class<POJO> pojoType) {
        this(pojoType, false);
    }

    public ObjectLookupDao(Class<POJO> pojoType, boolean getByIdWhenZero) {
        super(pojoType);
        this.naturalId = NaturalIdPojo.class.isAssignableFrom(pojoType);

        if (getByIdWhenZero && !this.naturalId) {
                throw new IllegalArgumentException("DbPojo class when getByIdWhenZero = true must be a NaturalIdPojo");
        }

        this.getByIdWhenZero = getByIdWhenZero;
        this.workingLookups = new WeakInstancesTracker<>(pojoType, pojoType, pojo -> pojo);
    }


    public POJO getOrCreate(POJO pojo) throws LookupException {
        if (pojo == null) {
            return null;
        }

        return workingLookups.getOrConstructWith(pojo, p -> {
            POJO result;

            if (this.naturalId && (this.getByIdWhenZero || p.getId() != 0)) {
                result = this.getById(p.getId());
                if (result != null) {
                    if (!p.equals(result)) {
                        this.saveOrUpdate(p);
                        result = this.getById(p.getId());

                        if (!p.equals(result)) {
                            throw new LookupException("Unable to correctly assign object lookup DbPojo to values:\n"
                                    + p.toString(), this.getPojoClass());
                        }
                    }
                    return result;
                }

            } else {
                result = this.findByExactPojo(p);
                if (result != null) {
                    return result;
                }
            }

            if (this.insert(p) != -1) {
                result = this.getById(p.getId());

                if (!p.equals(result)) {
                    throw new LookupException("Unable to correctly assign object lookup DbPojo to values:\n"
                            + p.toString(), this.getPojoClass());
                }

                return result;

            } else {
                throw new LookupException("Couldn't create pojo '" + p + "'",
                        this.getPojoClass());
            }
        });
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

    public static <P extends DbPojo, D extends Dao<P>> D get(Class<P> objectLookupPojoClass) {
        D dao = Dao.get(objectLookupPojoClass);
        if (dao != null && dao instanceof ObjectLookupDao) {
            return dao;

        } else {
            return null;
        }
    }
}
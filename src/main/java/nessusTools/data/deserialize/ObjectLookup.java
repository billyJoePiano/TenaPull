package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;


import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

import java.io.IOException;
import java.util.*;

public class ObjectLookup {
    private ObjectLookup() { }

    public static class Deserializer<POJO extends ObjectLookupPojo<POJO>>
            extends AbstractContextualPojoDeserializer<POJO, ObjectLookupDao<POJO>> {

        public Deserializer() { }

        public Deserializer(Class<POJO> pojoType) {
            this.setType(pojoType);
        }

        private static final Logger logger = LogManager.getLogger(ObjectLookup.Deserializer.class);
        public Logger getLogger() {
            return logger;
        }


        @Override
        public POJO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (this.dao == null) {
                logger.error("Could not find dao for '" + jp.getText() + "'");
                return null;
            }

            POJO searchPojo = jp.readValueAs(this.pojoType);


            try {
                return dao.getOrCreate(searchPojo); // TODO ??? what method to get the current object as JsonNode / JsonObject ???

            } catch (LookupException le) {
                logger.error("Error deserializing object lookup:");
                dao.getLogger().error(le);
                return null;
            }
        }
    }

    public static class ResponseChildLookupDeserializer
                    <POJO extends NessusResponse.ResponseChild<POJO, R> & ObjectLookupPojo<POJO>,
                        R extends NessusResponse>
                extends Deserializer<POJO> {

        private static final Logger logger = LogManager.getLogger(ObjectLookup.ResponseChildLookupDeserializer.class);
        public Logger getLogger() {
            return logger;
        }

        @Override
        public POJO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (this.dao == null) {
                logger.error("Could not find dao for '" + jp.getText() + "'");
                return null;
            }

            POJO searchPojo = jp.readValueAs(this.pojoType);

            Class<R> type = searchPojo._getResponseType();
            JsonStreamContext jsc = jp.getParsingContext();
            boolean success = false;

            while (jsc != null) {
                Object value = jsc.getCurrentValue();
                if (value != null && Objects.equals(value.getClass(), type)) {
                    searchPojo.setResponse((R)value);
                    success = true;
                    break;
                }
                jsc = jsc.getParent();
            }

            if (!success) {
                return searchPojo;
            }

            try {
                return dao.getOrCreate(searchPojo);

            } catch (LookupException le) {
                logger.error("Error deserializing object lookup:");
                dao.getLogger().error(le);
                return null;
            }
        }
    }



    /*
    public static class ListDeserializer<POJO extends DbPojo> {
        extends AbstractContextualDeserializer<List<POJO>, ObjectLookupDao<POJO>>

    }
    */


    //Serializer not needed
}

package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

import java.io.IOException;

public class ObjectLookup {
    private ObjectLookup() { }

    public static class Deserializer<POJO extends Pojo>
            extends AbstractContextualDeserializer<POJO, ObjectLookupDao<POJO>> {

        private static Logger logger = LogManager.getLogger(ObjectLookup.Deserializer.class);
        public Logger getLogger() {
            return logger;
        }


        @Override
        public POJO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (this.dao == null) {
                logger.error("Could not find dao for '" + jp.getText() + "'");
                return null;
            }

            POJO searchPojo = jp.readValueAs(this.pojoClass);

            try {
                return dao.getOrCreate(searchPojo); // TODO ??? what method to get the current object as JsonNode / JsonObject ???

            } catch (LookupException le) {
                dao.getLogger().error(le);
                return null;
            }
        }
    }

    /*
    public static class ListDeserializer<POJO extends Pojo> {
        extends AbstractContextualDeserializer<List<POJO>, ObjectLookupDao<POJO>>

    }
    */


    //Serializer not needed
}

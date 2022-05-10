package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.data.persistence.SimpleStringLookupDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Includes two inner classes for serializing / deserializing StringLookupPojos
 */
public class Lookup {
    private Lookup() { }

    /**
     * Converts a JSON string into the appropriate StringLookupPojo instance of
     * the type provided by the AbstractContextualPojoDeserializer super class
     * @param <POJO> The StringLookupPojo type
     * @param <DAO> The Dao for the StringLookupPojo type
     */
    public static class Deserializer<POJO extends StringLookupPojo<POJO>,
                                      DAO extends Dao<POJO> & StringLookupDao<POJO>>
            extends AbstractContextualPojoDeserializer<POJO, DAO> {

        private static final Logger logger = LogManager.getLogger(Deserializer.class);
        protected Logger getLogger() {
            return logger;
        }

        /**
         * Converts a JSON string into the appropriate StringLookupPojo instance of
         * the type provided by the AbstractContextualPojoDeserializer super class,
         * using the Dao provided for that StringLookupPojo type.
         * @param jp
         * @param ctxt
         * @return
         * @throws IOException
         */
        @Override
        public POJO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (this.dao == null) {
                logger.error("Could not find dao for '" + jp.getText() + "'");
                return null;
            }

            try {
                return dao.getOrCreate(jp.getText());

            } catch (LookupException le) {
                dao.getLogger().error(le);
                return null;
            }
        }
    }

    /**
     * Serializes a StringLookupPojo of any type into the JSON string that it represents
     *
     * @param <POJO> the StringLookupPojo type
     */
    public static class Serializer<POJO extends StringLookupPojo<POJO>> extends JsonSerializer<POJO> {
        /**
         * Serializes a StringLookupPojo of any type into the JSON string that it represents
         *
         * @param pojo
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
        @Override
        public void serialize(POJO pojo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (pojo != null) {
                jsonGenerator.writeString(pojo.getValue());

            } else {
                jsonGenerator.writeNull();
            }
        }
    }
}

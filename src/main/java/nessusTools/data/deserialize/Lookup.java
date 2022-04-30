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

public class Lookup {
    private Lookup() { }

    public static class Deserializer<POJO extends StringLookupPojo<POJO>,
                                      DAO extends Dao<POJO> & StringLookupDao<POJO>>
            extends AbstractContextualPojoDeserializer<POJO, DAO> {

        private static final Logger logger = LogManager.getLogger(Deserializer.class);
        protected Logger getLogger() {
            return logger;
        }

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

    public static class Serializer<POJO extends StringLookupPojo<POJO>> extends JsonSerializer<POJO> {
        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
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

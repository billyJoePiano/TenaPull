package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;
import nessusTools.data.persistence.LookupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Lookup {
    private Lookup() { }

    public static class Deserializer<POJO extends LookupPojo>
            extends AbstractContextualPojoDeserializer<POJO, LookupDao<POJO>> {

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

    public static class Serializer extends JsonSerializer<LookupPojo> {
        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
        @Override
        public void serialize(LookupPojo pojo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (pojo != null) {
                jsonGenerator.writeString(pojo.getValue());

            } else {
                jsonGenerator.writeNull();
            }
        }
    }
}

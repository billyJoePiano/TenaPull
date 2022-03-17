package nessusData.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import nessusData.entity.template.Pojo;
import nessusData.persistence.Dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class IdReference {
    private IdReference() { }

    public static class Serializer extends JsonSerializer<Pojo> {
        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
        @Override
        public void serialize(Pojo pojo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (pojo != null) {
                jsonGenerator.writeNumber(pojo.getId());

            } else {
                jsonGenerator.writeNull();
            }
        }
    }

    public static class Deserializer<POJO extends Pojo>
            extends AbstractContextualDeserializer<POJO, Dao<POJO>> {

        private static Logger logger = LogManager.getLogger(Deserializer.class);
        protected Logger getLogger() {
            return logger;
        }

        @Override
        public POJO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (this.dao == null) {
                logger.error("Could not find dao for '" + jp.getText() + "'");
                return null;
            }

            POJO pojo;
            Integer id = null;

            try {
                id = jp.getIntValue();
                pojo = dao.getById(id);

            } catch (IOException e) {
                dao.getLogger().error(e);
                return null;
            }

            if (id == null) {
                dao.getLogger().error("Could not get id for pojo");
                return null;
            }

            if (pojo == null) {
                try {
                    //construct a dummy placeholder
                    pojo = this.pojoClass.getDeclaredConstructor().newInstance();
                    pojo.setId(id);

                } catch (Exception e) {
                    dao.getLogger().error(e);
                    return null;
                }
            }

            return pojo;
        }
    }
}
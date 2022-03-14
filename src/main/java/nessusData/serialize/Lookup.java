package nessusData.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import nessusData.entity.template.LookupPojo;
import nessusData.persistence.LookupDao;
import nessusData.persistence.LookupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Lookup {
    private Lookup() { }

    public static class Deserializer
            extends JsonDeserializer<LookupPojo>
            implements ContextualDeserializer {

        private static Logger logger = LogManager.getLogger(Deserializer.class);

        private Class pojoClass = null;
        private LookupDao dao = null;

        @Override
        public LookupPojo deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
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

        // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
        @Override
        public JsonDeserializer<LookupPojo> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            JavaType type = deserializationContext.getContextualType() != null
                    ? deserializationContext.getContextualType()
                    : beanProperty.getMember().getType();

            if (type == null) {
                this.pojoClass = null;
                this.dao = null;
                logger.error("Null type returned for deserialization context");
                logger.error(deserializationContext);
                logger.error(beanProperty);

            } else {
                this.pojoClass = type.getRawClass();

                if (this.pojoClass == null) {
                    this.dao = null;
                    logger.error("Could not get raw class from 'JavaType' -- returned null");
                    logger.error(deserializationContext);
                    logger.error(beanProperty);
                    logger.error(type);

                } else {
                    this.dao = LookupDao.get(this.pojoClass);

                    if (this.dao == null) {
                        logger.error("Could not find dao for raw class");
                        logger.error(deserializationContext);
                        logger.error(beanProperty);
                        logger.error(type);
                        logger.error(this.pojoClass);
                    }
                }
            }

            return this;
        }
    }

    public static class Serializer extends JsonSerializer<LookupPojo> {
        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
        @Override
        public void serialize(LookupPojo pojo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(pojo != null ? pojo.toString() : null);
        }
    }
}

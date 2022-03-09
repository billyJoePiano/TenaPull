package nessusData.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import nessusData.entity.Pojo;
import nessusData.persistence.Dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class IdRefDeserializer
        extends JsonDeserializer<Pojo>
        implements ContextualDeserializer {

    private static Logger logger = LogManager.getLogger(IdRefDeserializer.class);

    private Class pojoClass = null;
    private Dao dao = null;

    @Override
    public Pojo deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (this.dao == null) {
            logger.error("Could not find dao for '" + jp.getText() + "'");
            return null;
        }

        Pojo pojo;
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
                pojo = (Pojo) this.pojoClass.getDeclaredConstructor().newInstance();
                pojo.setId(id);

            } catch (Exception e) {
                dao.getLogger().error(e);
                return null;
            }
        }

        return pojo;
    }

    // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
    @Override
    public JsonDeserializer<Pojo> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
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
                this.dao = Dao.get(this.pojoClass);

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
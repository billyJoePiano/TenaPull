package nessusData.serialize;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import nessusData.entity.template.Pojo;
import nessusData.persistence.Dao;
import org.apache.logging.log4j.Logger;

public abstract class AbstractContextualDeserializer<POJO extends Pojo, DAO extends Dao<POJO>>
        extends JsonDeserializer<POJO>
        implements ContextualDeserializer {

    protected Class<POJO> pojoClass = null;
    protected DAO dao = null;

    protected abstract Logger getLogger();


    // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
    @Override
    public JsonDeserializer<POJO> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        JavaType type = deserializationContext.getContextualType() != null
                ? deserializationContext.getContextualType()
                : beanProperty.getMember().getType();

        if (type == null) {
            this.pojoClass = null;
            this.dao = null;
            Logger logger = this.getLogger();
            logger.error("Null type returned for deserialization context");
            logger.error(deserializationContext);
            logger.error(beanProperty);

        } else {
            this.pojoClass = (Class<POJO>) type.getRawClass();

            if (pojoClass == null) {
                this.dao = null;
                Logger logger = this.getLogger();
                logger.error("Could not get raw class from 'JavaType' -- returned null");
                logger.error(type);
                logger.error(deserializationContext);
                logger.error(beanProperty);

            } else {
                this.dao = (DAO) Dao.get(pojoClass);

                if (this.dao == null) {
                    Logger logger = this.getLogger();
                    logger.error("Could not find dao for raw class");
                    logger.error(this.pojoClass);
                    logger.error(type);
                    logger.error(deserializationContext);
                    logger.error(beanProperty);
                }
            }
        }

        return this;
    }
}

package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

public abstract class AbstractContextualDeserializer<T>
        extends JsonDeserializer<T>
        implements ContextualDeserializer {

    private Class<T> type = null;
    private JavaType javaType = null;

    protected abstract Logger getLogger();

    public Class<T> getType() {
        return this.type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public JavaType getJavaType() {
        return this.javaType;
    }

    // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
    @Override
    public JsonDeserializer<T> createContextual(
                                        DeserializationContext deserializationContext,
                                        BeanProperty beanProperty)
            throws JsonMappingException {

        JavaType javaType = deserializationContext.getContextualType();
        if (javaType == null) {
            javaType = beanProperty.getMember().getType();
        }

        this.javaType = javaType;

        if (javaType == null) {
            this.type = null;
            Logger logger = this.getLogger();
            logger.error("Null type returned for deserialization context");
            logger.error(deserializationContext);
            logger.error(beanProperty);

        } else {
            this.type = (Class<T>) javaType.getRawClass();

            if (this.type == null) {
                Logger logger = this.getLogger();
                logger.error("Could not get raw class from 'JavaType' -- returned null");
                logger.error(javaType);
                logger.error(deserializationContext);
                logger.error(beanProperty);

            }
        }

        return this;
    }
}

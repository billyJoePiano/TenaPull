package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

/**
 * Implementation of Jackson's JsonDeserializer and ContextualDeserializer which
 * obtains the intended target type of the data to be deserialized through Jackson's
 * ContextualDeserializer.createContextual method.  Stores the result in the type property
 *
 *
 * @param <T> the type parameter
 */
public abstract class AbstractContextualDeserializer<T>
        extends JsonDeserializer<T>
        implements ContextualDeserializer {

    /**
     * The intended target type for deserialization
     */
    private Class<T> type = null;

    /**
     * The JavaType as provided by Jackson
     */
    private JavaType javaType = null;

    /**
     * Gets the logger for the implemented deserializer class
     *
     * @return the logger
     */
    protected abstract Logger getLogger();

    /**
     * Gets the target deserialization type
     *
     * @return the type
     */
    public Class<T> getType() {
        return this.type;
    }

    /**
     * Sets the target deserialization type
     *
     * @param type the type
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * Gets the JavaType provided by Jackson in the createContextual method
     *
     * @return the java type
     */
    public JavaType getJavaType() {
        return this.javaType;
    }

    /**
     * Obtains the intended target type for deserialization, via Jackson's JavaType
     *
     * See also: https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
     *
     * @param deserializationContext
     * @param beanProperty
     * @return this, since it serves a dual-purpose as both the contextual parser and the deserializer
     * @throws JsonMappingException
     */
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

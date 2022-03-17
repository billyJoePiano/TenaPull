package nessusData.serialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import nessusData.entity.template.*;
import nessusData.persistence.*;
import org.apache.logging.log4j.*;

import javax.json.JsonException;
import java.io.IOException;
import java.util.*;

public class ListContextualDeserializer<POJO extends Pojo>
        extends JsonDeserializer<List<POJO>>
        implements ContextualDeserializer {

    private Class<POJO> pojoClass = null;
    private AbstractContextualDeserializer<POJO, ? extends Dao<POJO>> deserializer = null;

    //For logging purposes only, in AbstractContextualDeserializer, via method setPojoClass()
    private BeanProperty beanProperty = null;
    private JavaType javaType = null;


    private static Logger logger = LogManager.getLogger(ListContextualDeserializer.class);

    @Override
    public List<POJO> deserialize(JsonParser jsonParser,
                                  DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {

        if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new JsonException("Expected start of array.  Got this instead: "
                    + jsonParser.currentToken());
        }

        jsonParser.clearCurrentToken();

        List<POJO> list = new ArrayList();

        if (this.deserializer != null) {
            this.deserializer.setPojoClass(this.pojoClass,
                    this.javaType, deserializationContext, this.beanProperty);

            JsonToken token;
            while ((token = jsonParser.nextToken()) != JsonToken.END_ARRAY) {
                list.add(deserializer.deserialize(jsonParser, deserializationContext));
                jsonParser.clearCurrentToken();
            }

        } else {
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                list.add(jsonParser.readValueAs(this.pojoClass));
                jsonParser.clearCurrentToken();
            }

            /*
            Iterator<POJO> iterator = jsonParser.readValuesAs(pojoClass);

            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
             */
        }

        return list;
    }

    // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
    @Override
    public JsonDeserializer<List<POJO>> createContextual(DeserializationContext deserializationContext,
                                                         BeanProperty beanProperty)
            throws JsonMappingException {

        //For logging purposes only, in AbstractContextualDeserializer, via method setPojoClass()
        this.beanProperty = beanProperty;
        this.javaType = deserializationContext.getContextualType() != null
                ? deserializationContext.getContextualType()
                : beanProperty.getMember().getType();


        ListType listType = beanProperty.getMember().getAnnotation(ListType.class);
        this.pojoClass = (Class<POJO>) listType.type();

        Class<AbstractContextualDeserializer<POJO, ? extends Dao<POJO>>> deserializerClass
                = (Class<AbstractContextualDeserializer<POJO, ? extends Dao<POJO>>>) listType.using();

        if (deserializerClass != null) {
            try {
                this.deserializer = deserializerClass.getDeclaredConstructor().newInstance();

            } catch (Exception e) {
                logger.error("Exception attempting to instantiate deserializer class " + deserializerClass.getName());
                logger.error(e);
            }
        }

        return this;
    }
}

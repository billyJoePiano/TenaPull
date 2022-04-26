package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

public class ResponseChildDeserializer<POJO extends NessusResponse.ResponseChild<POJO, R>,
                                        DAO extends Dao<POJO>,
                                          R extends NessusResponse>
            extends AbstractContextualPojoDeserializer<POJO, DAO> {

    public ResponseChildDeserializer() { }

    public ResponseChildDeserializer(Class<POJO> pojoType) {
        this.setType(pojoType);
    }

    private static final Logger logger = LogManager.getLogger(ResponseChildDeserializer.class);

    public Logger getLogger() {
        return logger;
    }

    @Override
    public POJO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (this.dao == null) {
            logger.error("Could not find dao for '" + jp.getText() + "'");
            return null;
        }

        POJO searchPojo = jp.readValueAs(this.pojoType);

        Class<R> type = searchPojo._getResponseType();
        JsonStreamContext jsc = jp.getParsingContext();

        while (jsc != null) {
            Object value = jsc.getCurrentValue();
            if (value != null && Objects.equals(value.getClass(), type)) {
                searchPojo.setResponse((R) value);
                break;
            }
            jsc = jsc.getParent();
        }

        return searchPojo;
    }
}

package nessusData.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.*;

import java.util.Objects;

public interface Pojo {
    public int getId();
    public void setId(int id);

    default public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public default boolean _equals(Object o) {
        if (o == this) return true;
        if (o == null || !o.getClass().equals(this.getClass())) return false;
        Pojo other = (Pojo) o;
        try {
            return this.toJson().equals(other.toJson());

        } catch (JsonProcessingException e) {
            Logger logger = LogManager.getLogger(this.getClass());
            logger.error("Exception while processing default _equals() method of Pojo interface:");
            logger.error(e);
            return false;
        }

    }

    public default String _toString() {
        try {
            return this.toJson();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for class '"
                    + this.getClass().toString() + "' :\n"
                    + e.getMessage();
        }
    }
}

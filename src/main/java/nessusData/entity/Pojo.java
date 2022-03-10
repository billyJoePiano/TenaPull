package nessusData.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.*;

public interface Pojo {
    public int getId();
    public void setId(int id);

    default public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    default public boolean _equals(Object o) {
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
}

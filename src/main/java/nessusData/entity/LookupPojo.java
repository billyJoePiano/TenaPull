package nessusData.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nessusData.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.IOException;

public interface LookupPojo extends Pojo {
    public String toString();
    public void setString(String string);

    default public boolean _equals(Object o) {
        if (o == this) {
            return true;

        } else if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        LookupPojo other = (LookupPojo) o;

        return      other.getId()       == this.getId()
                &&  other.toString()    == this.toString();
    }
}
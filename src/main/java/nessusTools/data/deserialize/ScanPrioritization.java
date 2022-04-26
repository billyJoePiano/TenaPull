package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.*;
import java.util.*;

public class ScanPrioritization extends NestedJsonArray<ScanResponse, ScanPlugin>
        implements ScanResponse.ScanResponseChild<ScanPrioritization> {

    private static Logger logger = LogManager.getLogger(ScanPrioritization.class);

    private static ResponseChildDeserializer<ScanPlugin, ObjectLookupDao<ScanPlugin>, ScanResponse>
            childDeserializer = new ResponseChildDeserializer<>(ScanPlugin.class);

    public static final String ARRAY_KEY = "plugins";

    @JsonProperty("threat_level")
    private Integer threatLevel;

    @Override
    public void putExtraJson(String key, Object value) {
        if (Objects.equals("threat_level", key)) {
            if (value != null) {
                if (value instanceof NumericNode) {
                    this.setThreatLevel(((NumericNode) value).asInt());

                } else if (value instanceof Number) {
                    if (value instanceof Integer) {
                        this.setThreatLevel((Integer)value);

                    } else {
                        this.setThreatLevel(((Number)value).intValue());
                    }

                } else try {
                    this.setThreatLevel(Integer.parseInt(value.toString()));

                } catch (NumberFormatException e) {
                    logger.error(e);
                    super.putExtraJson(key, value);
                }

            } else {
                this.setThreatLevel(null);
            }

        } else {
            super.putExtraJson(key, value);
        }
    }


    public static ScanPrioritization wrapIfNeeded(ScanResponse parent, List<ScanPlugin> item) {
        ScanPrioritization wrapped = NestedJsonArray.wrapIfNeeded(parent, item, ScanPrioritization.class);
        if (wrapped == item) {
            parent.setThreatLevel(wrapped.threatLevel);

        } else {
            wrapped.threatLevel = parent.getThreatLevel();
        }
        return wrapped;
    }

    public ScanPrioritization() { }

    public ScanPrioritization(List<ScanPlugin> item) {
        super(item);
    }

    @Override
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonProperty("plugins")
    public List<ScanPlugin> getPlugins() {
        return this.getList();
    }

    @JsonProperty("plugins")
    public void setPlugins(List<ScanPlugin> item) {
        this.setList(item);
    }


    public static class Deserializer extends DeserializerTemplate<ScanPrioritization, ScanPlugin> {
        @Override
        public JsonDeserializer<ScanPlugin> getChildDeserializer() {
            return childDeserializer;
        }

        @Override
        public ScanPrioritization getNewWrapperInstance() {
            return new ScanPrioritization();
        }
    }

    public static class Serializer extends SerializerTemplate<ScanPrioritization, ScanPlugin> { }

    protected void writeBeforeArray(JsonGenerator jg, SerializerProvider sp) throws IOException {
        jg.writeFieldName("threat_level");
        Integer threatLevel = this.getThreatLevel();
        if (threatLevel != null) {
            jg.writeNumber(threatLevel);

        } else {
            jg.writeNull();
        }
    }

    @JsonIgnore
    ScanResponse response;

    @JsonIgnore
    @Override
    public int getId() {
        return this.response != null ? this.response.getId() : 0;
    }

    @JsonIgnore
    @Override
    public void setId(int id) {
        /*if (this.response != null) {
            this.response.setId(0);
        }*/
    }

    public Integer getThreatLevel() {
        ScanResponse response = this.getResponse();
        if (response != null) return response.getThreatLevel();
        return this.threatLevel;
    }

    public void setThreatLevel(Integer threatLevel) {
        this.threatLevel = threatLevel;
        if (response != null) response.setThreatLevel(threatLevel);
    }

    @Override
    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this, ObjectNode.class);
    }

    @Override
    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    @Override
    public ScanResponse getResponse() {
        return this.response;
    }

    @JsonIgnore
    @Override
    public void setResponse(ScanResponse response) {
        this.response = response;
    }
}

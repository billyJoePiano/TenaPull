package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.sql.*;

@MappedSuperclass
@JsonIgnoreProperties(allowSetters = true, value = {"id"})
public abstract class NessusResponseGenerateTimestamp<RES extends NessusResponseGenerateTimestamp<RES>>
        extends NaturalIdPojo
        implements NessusResponse<RES> { // (NessusResponse extends DbPojo)

    @JsonIgnore
    public abstract String getUrlPath();

    @UpdateTimestamp
    @JsonIgnore
    private Timestamp timestamp;

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Transient
    @JsonIgnore
    private IdCachingSerializer.MainCachedNode<RES> cachedNode;

    public IdCachingSerializer.MainCachedNode<RES> getCachedNode() {
        return this.cachedNode;
    }

    public void setCachedNode(IdCachingSerializer.MainCachedNode<RES> cachedNode) {
        if (cachedNode != null) {
            assert cachedNode.getId() == this.getId() && cachedNode.represents((RES)this);
        }
        this.cachedNode = cachedNode;
    }

    public static JsonSerializer
        getCachingSerializer(JsonSerializer defaultSerializer, ObjectMapper mapper) {

        return IdCachingSerializer.getIdCachingSerializer(defaultSerializer, mapper);
    }

    public static JsonSerializer
            getCacheResetSerializer(JsonSerializer defaultSerializer, ObjectMapper mapper) {

        return IdCachingSerializer.getCacheResetSerializer(defaultSerializer, mapper);
    }

    @Override
    public final ObjectNode toJsonNode() {
        if (this.cachedNode == null){
            if (this.getId() == 0) {
                return super.toJsonNode();
            }
            this.cachedNode = IdCachingSerializer.getOrCreateNodeCache((RES)this);
        }
        return this.cachedNode.getNode();
    }

    @Override
    public final String toJsonString() throws JsonProcessingException {
        if (this.cachedNode == null){
            if (this.getId() == 0) {
                return super.toJsonString();
            }
            this.cachedNode = IdCachingSerializer.getOrCreateNodeCache((RES)this);
        }
        return this.cachedNode.getString();
    }
}

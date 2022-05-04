package nessusTools.data.entity.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;

import javax.persistence.*;
import javax.persistence.AccessType;

@MappedSuperclass
public interface NessusResponse<RES extends NessusResponse<RES>>
        extends IdCachingSerializer.NodeCacher<RES> {
    public String getUrlPath();

    public Timestamp getTimestamp();

    public void setTimestamp(Timestamp timestamp);

    public interface ResponseChild
                <POJO extends ResponseChild<POJO, R>,
                    R extends NessusResponse>
            extends DbPojo {

        public R getResponse();
        public void setResponse(R response);
    }


    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class SingleChildTemplate
                        <POJO extends SingleChildTemplate<POJO, R>,
                            R extends NessusResponse>
            extends NaturalIdPojo
            implements ResponseChild<POJO, R>,
                        IdCachingSerializer.NodeCacher<POJO> {

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id")
        @Access(AccessType.PROPERTY)
        @JsonIgnore
        private R response;

        public R getResponse() {
            return this.response;
        }

        public void setResponse(R response) {
            this.response = response;
            if (response == null) this.setId(0);
            else this.setId(response.getId());
        }

        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            POJO other = (POJO)o;
            R theirs = other.getResponse();
            if (this.response == null) {
                return theirs == null;

            } else if (theirs == null) {
                return false;
            }
            return this.response.getId() == theirs.getId();
        }

        @Transient
        @JsonIgnore
        protected void __set(POJO o) {
            super.__set(o);
            this.setResponse((R)o.getResponse());
        }

        @Transient
        @JsonIgnore
        private IdCachingSerializer.MainCachedNode<POJO> cachedNode;

        public IdCachingSerializer.MainCachedNode<POJO> getCachedNode() {
            return this.cachedNode;
        }

        public void setCachedNode(IdCachingSerializer.MainCachedNode<POJO> cachedNode) {
            if (cachedNode != null) {
                assert cachedNode.getId() == this.getId() && cachedNode.represents((POJO)this);
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
    }


    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class MultiChildTemplate
            <POJO extends MultiChildTemplate<POJO, R>,
                    R extends NessusResponse>
            extends GeneratedIdPojo
            implements ResponseChild<POJO, R>,
                        IdCachingSerializer.NodeCacher<POJO> {

        @ManyToOne(fetch = FetchType.LAZY)
        @Access(AccessType.PROPERTY)
        @JsonIgnore
        private R response;

        public R getResponse() {
            return this.response;
        }

        public void setResponse(R response) {
            this.response = response;
        }

        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            POJO other = (POJO)o;
            R theirs = other.getResponse();
            if (this.response == null) {
                return theirs == null;

            } else if (theirs == null) {
                return false;
            }
            return this.response.getId() == theirs.getId();
        }

        @Transient
        @JsonIgnore
        protected void __set(POJO o) {
            super.__set(o);
            this.setResponse((R)o.getResponse());
        }

        @Transient
        @JsonIgnore
        protected boolean __match(POJO other) {
            if (other == null) return false;
            if (other == this) return true;
            R myRes = this.getResponse();
            R theirRes = other.getResponse();
            if (myRes == null || theirRes == null) {
                return false;
            }

            if (myRes != theirRes) {
                int myId = myRes.getId();
                int theirId = theirRes.getId();

                if (myId == 0 || myId != theirId) {
                    return false;
                }
            }
            return true;
        }

        @Transient
        @JsonIgnore
        private IdCachingSerializer.MainCachedNode<POJO> cachedNode;

        public IdCachingSerializer.MainCachedNode<POJO> getCachedNode() {
            return this.cachedNode;
        }

        public void setCachedNode(IdCachingSerializer.MainCachedNode<POJO> cachedNode) {
            if (cachedNode != null) {
                assert cachedNode.getId() == this.getId() && cachedNode.represents((POJO)this);
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
    }

}

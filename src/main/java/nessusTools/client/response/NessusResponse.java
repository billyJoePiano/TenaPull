package nessusTools.client.response;

import java.lang.reflect.*;
import java.sql.Timestamp;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.*;
import nessusTools.data.entity.template.*;
import nessusTools.sync.*;
import org.apache.logging.log4j.*;
import org.hibernate.annotations.*;

import javax.persistence.*;

@MappedSuperclass
public interface NessusResponse extends DbPojo {
    public String getUrlPath();

    public Timestamp getTimestamp();

    public void setTimestamp(Timestamp timestamp);


    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class ChildTemplate<R extends NessusResponse> extends NaturalIdPojo {
        @Transient
        @JsonIgnore
        private R response;

        @Transient
        @JsonIgnore
        public R _getResponse() {
            return this.response;
        }

        @Transient
        @JsonIgnore
        public void _setResponse(R response) {
            this.response = response;
        }

        @Transient
        @JsonIgnore
        public abstract Class<R> _getResponseType();

        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            ChildTemplate other = (ChildTemplate)o;
            if (this.response == null) {
                return other.response == null;

            } else if (other.response == null) {
                return false;
            }
            return this.response.getId() == other.response.getId();
        }
    }

    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class ChildLookupTemplate<POJO extends ChildLookupTemplate, R extends NessusResponse>
            extends GeneratedIdPojo implements ObjectLookupPojo<POJO> {

        @Transient
        @JsonIgnore
        private R response;

        @Transient
        @JsonIgnore
        public R _getResponse() {
            return this.response;
        }

        @Transient
        @JsonIgnore
        public void _setResponse(R response) {
            this.response = response;
        }

        @Transient
        @JsonIgnore
        public abstract Class<R> _getResponseType();

        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            ChildTemplate other = (ChildTemplate)o;
            if (this.response == null) {
                return other.response == null;

            } else if (other.response == null) {
                return false;
            }
            return this.response.getId() == other.response.getId();
        }

        @Transient
        @JsonIgnore
        public void _set(POJO o) {
            this.setId(o.getId());
            this._setResponse((R)o._getResponse());
            this.setExtraJson(o.getExtraJson());
        }
    }

}

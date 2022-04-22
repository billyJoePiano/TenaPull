package nessusTools.data.entity.template;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@MappedSuperclass
public interface NessusResponse extends DbPojo {
    public String getUrlPath();

    public Timestamp getTimestamp();

    public void setTimestamp(Timestamp timestamp);

    public interface ResponseChild
            <POJO extends ResponseChild<POJO, R>,
                    R extends NessusResponse> {

        public R getResponse();
        public void setResponse(R response);
        public Class<R> _getResponseType();
    }


    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class SingleChildTemplate
                        <POJO extends SingleChildTemplate<POJO, R>,
                            R extends NessusResponse>
            extends NaturalIdPojo
            implements ResponseChild<POJO, R> {

        @OneToOne
        @JoinColumn(name = "id")
        @JsonIgnore
        private R response;

        @JsonIgnore
        public R getResponse() {
            return this.response;
        }

        @JsonIgnore
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
    }

    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class SingleChildLookupTemplate
                        <POJO extends SingleChildLookupTemplate<POJO, R>,
                            R extends NessusResponse>

            extends SingleChildTemplate<POJO, R>
            implements ObjectLookupPojo<POJO> {

        @Transient
        @JsonIgnore
        protected void __set(POJO o) {
            this.setId(o.getId());
            this.setResponse((R)o.getResponse());
            this.setExtraJson(o.getExtraJson());
        }
    }


    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class MultiChildTemplate
            <POJO extends MultiChildTemplate<POJO, R>,
                    R extends NessusResponse>
            extends GeneratedIdPojo
            implements ResponseChild<POJO, R> {

        @ManyToOne
        @JsonIgnore
        private R response;

        @JsonIgnore
        public R getResponse() {
            return this.response;
        }

        @JsonIgnore
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
    }

    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class MultiChildLookupTemplate
            <POJO extends MultiChildLookupTemplate<POJO, R>,
                    R extends NessusResponse>
            extends MultiChildTemplate<POJO, R>
            implements ObjectLookupPojo<POJO> {

        @Transient
        @JsonIgnore
        protected void __set(POJO o) {
            this.setId(o.getId());
            this.setResponse((R)o.getResponse());
            this.setExtraJson(o.getExtraJson());
        }
    }

}

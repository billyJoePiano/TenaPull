package nessusTools.data.entity.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;

import javax.persistence.*;
import javax.persistence.AccessType;

@MappedSuperclass
public interface NessusResponse extends DbPojo {
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
            implements ResponseChild<POJO, R> {

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
    }


    @MappedSuperclass
    @JsonIgnoreProperties({"id"})
    public static abstract class MultiChildTemplate
            <POJO extends MultiChildTemplate<POJO, R>,
                    R extends NessusResponse>
            extends GeneratedIdPojo
            implements ResponseChild<POJO, R> {

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
    }

}

package tenapull.data.entity.response;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.entity.template.*;

import javax.persistence.*;
import javax.persistence.AccessType;

/**
 * Interface representing a response from the Nessus API, with some utility abstract classes
 * that can be implemented by nested objects within a response, in order to link those
 * objects to the parent response entity
 *
 * @param <RES> the Response type implementing NessusResponse
 */
@MappedSuperclass
public interface NessusResponse<RES extends NessusResponse<RES>> extends DbPojo {
    /**
     * Gets url path for the Nessus API
     *
     * @return the url path
     */
    public String getUrlPath();

    /**
     * Gets timestamp.  This may be the one provided by the Nessus API, or one generated
     * by this application, depending on the nature of the Nessus API response
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp();

    /**
     * Sets the Timestamp
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Timestamp timestamp);

    /**
     * The interface Response child, which can be implemented by any
     * nested child entities within the parent response
     *
     * @param <POJO> the type parameter
     * @param <R>    the type parameter
     */
    public interface ResponseChild
                <POJO extends ResponseChild<POJO, R>,
                    R extends NessusResponse>
            extends DbPojo {

        /**
         * Gets parent response.
         *
         * @return the response
         */
        public R getResponse();

        /**
         * Sets parent response.
         *
         * @param response the response
         */
        public void setResponse(R response);
    }


    /**
     * Represents a nested entity with a one-to-one relationship with the response
     *
     * @param <POJO> the child pojo type, implementing SingleChildTemplate
     * @param <R>    the parent response type, implementing NessusResponse
     */
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

        /**
         * Convenience method for subclass implementations wishing to implement
         * one of the object lookup interfaces.  Includes a call to NaturalIdPojo.__set(other)
         * and then sets the response object to match as well
         *
         * @param o the other pojo representing the same db record as this one
         */
        @Transient
        @JsonIgnore
        protected void __set(POJO o) {
            super.__set(o);
            this.setResponse((R)o.getResponse());
        }
    }


    /**
     * Represents a nested entity with a Many-to-one relationship with the response
     *
     * @param <POJO> the child pojo type, implementing SingleChildTemplate
     * @param <R>    the parent response type, implementing NessusResponse
     */
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

        /**
         * Convenience method for subclass implementations wishing to implement
         * one of the object lookup interfaces.  Includes a call to the ExtensibleJson __set()
         * and then sets the response object to match as well
         *
         * @param o the other pojo representing the same db record as this one
         */
        @Transient
        @JsonIgnore
        protected void __set(POJO o) {
            super.__set(o);
            this.setResponse((R)o.getResponse());
        }

        /**
         * Convienence method for subclass implementations wishing to implement
         * one of the object lookup interfaces, which checks the response ids
         * against each other.  It is assumed that the multi child lookups
         * will be using a composite key that includes the response id as well
         * as another id from the Nessus API
         *
         * @param other the other
         * @return the boolean
         */
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
    }
}

package nessusTools.client.response;

import java.sql.Timestamp;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.Pojo;

import javax.persistence.*;

@MappedSuperclass
public abstract class NessusResponse {
    public abstract List<PojoData> getData();
    public abstract void setData(PojoData data);

    public abstract Timestamp getTimestamp();
    public abstract void setTimestamp(Timestamp timestamp);

    @JsonIgnore
    private Map<String, JsonNode> _extraJson;

    @JsonAnyGetter
    public Map<String, JsonNode> _getExtraJson() {
        return this._extraJson;
    }

    @JsonAnySetter
    public void set_extraJson(String key, Object value) {
        if (this._extraJson == null) {
            this._extraJson = new HashMap();
        }
        this._extraJson.put(key, new ObjectMapper().convertValue(value, JsonNode.class));
    }

    public static class PojoData<POJO extends Pojo> {
        private final String fieldName;
        private final Class<POJO> pojoClass;
        private boolean isList;
        private List<POJO> pojoList = null;
        private POJO individualPojo = null;

        public PojoData(String fieldName, Class<POJO> pojoClass, List<POJO> pojoList) {
            this.fieldName = fieldName;
            this.pojoClass = pojoClass;
            this.pojoList = pojoList;
            this.isList = true;
        }

        public PojoData(String fieldName, Class<POJO> pojoClass, POJO individualPojo) {
            this.fieldName = fieldName;
            this.pojoClass = pojoClass;
            this.individualPojo = individualPojo;
            this.isList = false;
        }

        public PojoData(String fieldName, Class<POJO> pojoClass, boolean isList) {
            this.fieldName = fieldName;
            this.pojoClass = pojoClass;
            this.isList = isList;
        }

        public String getFieldName() {
            return this.fieldName;
        }

        public void setPojoList(List<POJO> pojoList) {
            if (!this.isList) {
                throw new UnsupportedOperationException("Cannot set list for an individual data type");
            }
            this.pojoList = pojoList;
        }

        public List<POJO> getPojoList() {
            if (!this.isList) {
                throw new UnsupportedOperationException("Cannot set list for an individual data type");
            }
            return this.pojoList;
        }

        public POJO getIndividualPojo() {
            if (this.isList) {
                throw new UnsupportedOperationException("Cannot set individual pojo for a list data type");
            }
            return individualPojo;
        }

        public void setIndividualPojo(POJO individualPojo) {
            if (this.isList) {
                throw new UnsupportedOperationException("Cannot set individual pojo for a list data type");
            }
            this.individualPojo = individualPojo;
        }

        public boolean isList() {
            return this.isList;
        }

        public boolean isIndividual() {
            return !this.isList;
        }

        public Class<POJO> getPojoClass() {
            return this.pojoClass;
        }
    }
}

package nessusData.serialize;

import java.sql.Timestamp;
import java.util.*;
import nessusData.entity.*;

public interface Response {
    public List<PojoData> getData();
    public void setData(PojoData data);

    public Timestamp getTimestamp();
    public void setTimestamp(Timestamp timestamp);

    public class PojoData<POJO extends Pojo> {
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

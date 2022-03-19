package nessusTools.client.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nessusTools.data.entity.ScanInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class ScanInfoResponse extends NessusResponse {
    private static Logger logger = LogManager.getLogger(ScanInfoResponse.class);

    private ScanInfo info;

    public ScanInfo getInfo() {
        return info;
    }

    public void setInfo(ScanInfo info) {
        this.info = info;
    }

    @JsonIgnore
    public Timestamp getTimestamp() {
        return null;
    }

    @JsonIgnore
    public void setTimestamp(Timestamp timestamp) { }


    @JsonIgnore
    public List<PojoData> getData() {
        return List.of(
                new PojoData<ScanInfo>("info", ScanInfo.class, this.getInfo())
            );
    }

    @JsonIgnore
    public void setData(PojoData data) {
        String fieldName = data.getFieldName();
        if (Objects.equals("info", fieldName)) {
            if (Objects.equals(ScanInfo.class, data.getPojoClass())) {
                this.setInfo((ScanInfo) data.getIndividualPojo());
                return;
            }
        }

        logger.error("Could not set data in ScanInfoResponse.setData");
        logger.error(data);
    }
}

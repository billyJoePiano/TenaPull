package nessusTools.client.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.ser.impl.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class ScanInfoResponse extends NessusResponse {
    public static Logger logger = LogManager.getLogger(ScanInfoResponse.class);

    public static String pathFor(int scanId) {
        return "/scans/" + scanId;
    }

    public static String pathFor(Scan scan) {
        if (scan != null) {
            return pathFor(scan.getId());

        } else {
            return null;
        }
    }

    public static String pathFor(ScanInfo scanInfo) {
        if (scanInfo != null) {
            return pathFor(scanInfo.getId());

        } else {
            return null;
        }
    }

    /*
    static {
        new SimpleFilterProvider().addFilter("SkipNull.Filter", new SkipNull.Filter());

    }
     */

    private ScanInfo info;

    public ScanInfo getInfo() {
        return info;
    }

    public void setInfo(ScanInfo info) {
        this.info = info;
        if (info == null) {
            return;
        }

        Integer responseId = this.getId();
        int infoId = info.getId();
        if (infoId == 0) {
            if (responseId != null) {
                info.setId(responseId);
            }

        } else if (infoId > 0) {
            this.setId(infoId);
        }
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

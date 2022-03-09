package nessusData.serialize;

import java.util.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import java.sql.Timestamp;

import nessusData.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IndexResponse implements Response {
    private static Logger logger = LogManager.getLogger(IndexResponse.class);

    private List<Folder> folders;

    private List<Scan> scans;

    @JsonDeserialize(using = EpochTimestampDeserializer.class)
    @JsonSerialize(using = EpochTimestampSerializer.class)
    private Timestamp timestamp;

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Scan> getScans() {
        return scans;
    }

    public void setScans(List<Scan> scans) {
        this.scans = scans;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public List<PojoData> getData() {
        return List.of(
                new PojoData<Folder>("folders", Folder.class, this.getFolders()),
                new PojoData<Scan>("scans", Scan.class, this.getScans())
            );
    }

    @JsonIgnore
    public void setData(PojoData data) {
        String fieldName = data.getFieldName();
        if ("folders".equals(fieldName)) {
            if (Objects.equals(Folder.class, data.getPojoClass())) {
                this.setFolders(data.getPojoList());
                return;
            }

        } else if ("scans".equals(fieldName)) {
            if (Objects.equals(Scan.class, data.getPojoClass())) {
                this.setScans(data.getPojoList());
                return;
            }
        }

        logger.error("Could not set data in IndexResponse.setData");
        logger.error(data);
    }
}

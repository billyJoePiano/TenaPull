package nessusTools.client.response;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;

import java.sql.Timestamp;

import nessusTools.data.entity.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IndexResponse extends NessusResponseWithTimestamp {
    public static final Logger logger = LogManager.getLogger(IndexResponse.class);

    public static String pathFor() {
        return "/scans";
    }

    private List<Folder> folders;

    private List<Scan> scans;

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
}

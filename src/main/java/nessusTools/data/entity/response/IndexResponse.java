package nessusTools.data.entity.response;

import java.util.*;

import nessusTools.data.entity.scan.*;
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

    @Override
    public String getUrlPath() {
        return "/scans";
    }
}

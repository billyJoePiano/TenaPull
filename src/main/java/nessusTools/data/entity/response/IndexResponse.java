package nessusTools.data.entity.response;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity(name = "IndexResponse")
@Table(name = "index_response")
public class IndexResponse extends NessusResponseWithTimestamp {
    public static final Dao<IndexResponse>
            dao = new Dao<IndexResponse>(IndexResponse.class);

    public static final Logger logger = LogManager.getLogger(IndexResponse.class);

    public static String pathFor() {
        return "/scans";
    }


    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "index_response_folder",
            joinColumns = { @JoinColumn(name = "response_id") },
            inverseJoinColumns = { @JoinColumn(name = "folder_id") }
    )
    @OrderColumn(name = "__order_for_index_response_folder", nullable = false)
    private List<Folder> folders;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "index_response_scan",
            joinColumns = { @JoinColumn(name = "response_id") },
            inverseJoinColumns = { @JoinColumn(name = "scan_id") }
    )
    @OrderColumn(name = "__order_for_index_response_scan", nullable = false)
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
    @Transient
    @JsonIgnore
    public String getUrlPath() {
        return "/scans";
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() { }
}

package nessusTools.data.entity;

import javax.persistence.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.Dao;
import org.apache.logging.log4j.*;

@Entity(name = "Folder")
@Table(name = "folder")
public class Folder extends NaturalIdPojo {
    public static final Dao<Folder> dao
            = new Dao<Folder>(Folder.class);

    public static final Logger logger = LogManager.getLogger(Folder.class);

    @Column
    private String name;

    @Column
    private String type;

    @Column(name = "default_tag")
    @JsonProperty("default_tag")
    private Integer defaultTag;

    @Column
    private Integer custom;

    @Column(name = "unread_count")
    @JsonProperty("unread_count")
    private Integer unreadCount;

    @OneToMany(mappedBy="folder", cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Scan> scans;

    public Folder() { }

    public Folder(int id,
                  String name,
                  String type,
                  Integer defaultTag,
                  Integer custom,
                  Integer unreadCount,
                  Set<Scan> scans) {

        this.setId(id);
        this.name = name;
        this.type = type;
        this.defaultTag = defaultTag;
        this.custom = custom;
        this.unreadCount = unreadCount;
        this.scans = scans;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDefaultTag() {
        return defaultTag;
    }

    public void setDefaultTag(Integer defaultTag) {
        this.defaultTag = defaultTag;
    }

    public Integer getCustom() {
        return custom;
    }

    public void setCustom(Integer custom) {
        this.custom = custom;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Set<Scan> getScans() {
        return scans;
    }

    public void setScans(Set<Scan> scans) {
        this.scans = scans;
    }
}
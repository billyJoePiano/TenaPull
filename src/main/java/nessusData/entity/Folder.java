package nessusData.entity;

import javax.persistence.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nessusData.persistence.Dao;
import nessusData.persistence.*;

@Entity(name = "Folder")
@Table(name = "folder")
public class Folder implements Pojo {
    public static final Dao<Folder> dao = new Dao<Folder>(Folder.class);

    @Id
    private int id;

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

    @OneToMany(mappedBy="folder", /*cascade = CascadeType.ALL,*/ orphanRemoval = false, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Scan> scans;

    public Folder() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
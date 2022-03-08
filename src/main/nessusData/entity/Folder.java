package main.nessusData.entity;

import javax.persistence.*;
import java.util.*;
import main.nessusData.persistence.*;

@Entity(name = "Folder")
@Table(name = "folder")
public class Folder {
    public static final Dao<Folder> dao = new Dao<Folder>(Folder.class);

    @Id
    private int id;

    @Column
    private String name;

    @Column
    private String type;

    @Column
    private Integer default_tag;

    @Column
    private Integer custom;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @OneToMany(mappedBy="folder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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

    public Integer getDefault_tag() {
        return default_tag;
    }

    public void setDefault_tag(Integer default_tag) {
        this.default_tag = default_tag;
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
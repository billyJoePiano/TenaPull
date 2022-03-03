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
    private int default_tag;

    @Column
    private int custom;

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

    public int getDefault_tag() {
        return default_tag;
    }

    public void setDefault_tag(int default_tag) {
        this.default_tag = default_tag;
    }

    public int getCustom() {
        return custom;
    }

    public void setCustom(int custom) {
        this.custom = custom;
    }

    public Set<Scan> getScans() {
        return scans;
    }

    public void setScans(Set<Scan> scans) {
        this.scans = scans;
    }
}
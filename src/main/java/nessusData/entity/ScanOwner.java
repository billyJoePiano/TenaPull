package nessusData.entity;

import nessusData.persistence.LookupDao;
import nessusData.persistence.*;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

@Entity(name = "ScanOwner")
@Table(name = "scan_owner")
public class ScanOwner implements LookupPojo {
    public static final LookupDao<ScanOwner> dao
            = new LookupDao<ScanOwner>(ScanOwner.class, "owner");

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private int id;

    @Column
    @NaturalId
    private String owner;

    public ScanOwner() { }

    public ScanOwner(String owner) {
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setString(String owner) {
        this.setOwner(owner);
    }

    public String toString() {
        return getOwner();
    }

    public boolean equals(Object o) {
        return LookupPojo.super._equals(o);
    }
}
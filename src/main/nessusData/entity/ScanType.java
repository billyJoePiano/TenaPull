package main.nessusData.entity;

import main.nessusData.persistence.*;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

@Entity(name = "ScanType")
@Table(name = "scan_type")
public class ScanType implements LookupPojo  {
    public static final LookupDao<ScanType> dao =
            new LookupDao<ScanType>(ScanType.class, "type");

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private int id;

    @Column
    @NaturalId
    private String type;

    public ScanType() { }

    public ScanType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setString(String type) {
        this.setType(type);
    }

    public String toString() {
        return getType();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;

        } else if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        ScanType other = (ScanType) o;

        return      other.getId()       == this.getId()
                &&  other.getType() == this.getType();
    }
}
package main.nessusData.entity;

import main.nessusData.persistence.*;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

@Entity(name = "Timezone")
@Table(name = "timezone")
public class Timezone implements LookupPojo  {
    public static final LookupDao<Timezone> dao
            = new LookupDao<Timezone>(Timezone.class, "timezone");

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private int id;

    @Column
    @NaturalId
    private String timezone;

    public Timezone() { }

    public Timezone(String timezone) {
        this.timezone = timezone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setString(String timezone) {
        this.setTimezone(timezone);
    }

    public String toString() {
        return getTimezone();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;

        } else if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        Timezone other = (Timezone) o;

        return      other.getId()       == this.getId()
                &&  other.getTimezone() == this.getTimezone();
    }
}
package nessusData.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nessusData.entity.template.*;
import nessusData.persistence.Dao;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "ScanGroup")
@Table(name = "scan_group")
public class ScanGroup extends NaturalIdPojo {
    public static final Dao<ScanGroup> dao
            = new Dao<ScanGroup>(ScanGroup.class);

}
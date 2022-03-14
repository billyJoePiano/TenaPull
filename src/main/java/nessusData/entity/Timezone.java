package nessusData.entity;

import nessusData.entity.template.LookupPojo;
import nessusData.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "Timezone")
@Table(name = "timezone")
public class Timezone extends LookupPojo {
    public static final LookupDao<Timezone> dao
            = new LookupDao<Timezone>(Timezone.class);
}
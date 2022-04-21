package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "PluginRefInformation")
@Table(name = "plugin_ref_information")
public class PluginRefInformation extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginRefInformation> {

    public static final ObjectLookupDao<PluginRefInformation> dao
            = new ObjectLookupDao<PluginRefInformation>(PluginRefInformation.class);

    String name;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "plugin_ref_information_value",
            joinColumns = { @JoinColumn(name = "information_id") },
            inverseJoinColumns = { @JoinColumn(name = "value_id") }
    )
    @OrderColumn(name = "__order_for_plugin_ref_value", nullable = false)
    List<PluginRefValue> values; //TODO flatten JSON

    String url;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PluginRefValue> getValues() {
        return values;
    }

    public void setValues(List<PluginRefValue> values) {
        this.values = values;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void _set(PluginRefInformation o) {
        this.setId(o.getId());
        this.name = o.name;
        this.values = o.values;
        this.url = o.url;
        this.setExtraJson(o.getExtraJson());
    }
}
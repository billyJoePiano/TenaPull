package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "PluginRefInformation")
@Table(name = "plugin_ref_information")
public class PluginRefInformation extends HashLookupTemplate<PluginRefInformation> {

    public static final HashLookupDao<PluginRefInformation> dao
            = new HashLookupDao<PluginRefInformation>(PluginRefInformation.class);

    String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @JoinTable(
            name = "plugin_ref_information_value",
            joinColumns = { @JoinColumn(name = "information_id") },
            inverseJoinColumns = { @JoinColumn(name = "value_id") }
    )
    @OrderColumn(name = "__order_for_plugin_ref_value", nullable = false)
    @JsonIgnore
    List<PluginRefValue> values;

    @Transient
    @JsonProperty("values")
    RefValues valuesJson;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String url;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Transient
    @JsonIgnore
    @Override
    public void _set(PluginRefInformation o) {
        this.__set(o);
        this.name = o.name;
        this.values = o.values;
        this.url = o.url;

        if (this.valuesJson != null) {
            this.valuesJson.clearParent();
            this.valuesJson = null;
        }
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginRefInformation o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.name, o.name)
                && Objects.equals(this.values, o.values)
                && Objects.equals(this.url, o.url)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PluginRefValue> getValues() {
        return this.values;
    }

    public void setValues(List<PluginRefValue> values) {
        if (this.values == values) return;
        this.values = values;
        if (this.valuesJson != null) this.valuesJson.setValue(this.values);
    }

    public RefValues getValuesJson() {
        if (this.valuesJson == null) {
            this.valuesJson = new RefValues();
            this.valuesJson.takeFieldsFromParent(this);
        }
        return this.valuesJson;
    }

    public void setValuesJson(RefValues values) {
        if (this.valuesJson != null && this.valuesJson != values) {
            this.valuesJson.clearParent();
        }
        this.valuesJson = values;
        if (values != null) values.putFieldsIntoParent(this);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @JsonAnyGetter
    @Transient
    @Override
    public Map<String, JsonNode> getExtraJsonMap() {
        return this.getValuesJson().jsonAnyGetterForParent();
    }


    @JsonAnySetter
    @Transient
    @Override
    public void putExtraJson(String key, Object value) {
        super.putExtraJson(key, value);
        if (this.valuesJson != null) this.valuesJson.checkExtraJsonPut(key, value);
    }
}
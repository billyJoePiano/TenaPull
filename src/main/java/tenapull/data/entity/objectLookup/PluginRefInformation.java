package tenapull.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Represents a reusable "object lookup", for the "ref" object included
 * in the plugin attributes returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "PluginRefInformation")
@Table(name = "plugin_ref_information")
public class PluginRefInformation extends HashLookupTemplate<PluginRefInformation> {

    /**
     * The dao for PluginRefInformation
     */
    public static final HashLookupDao<PluginRefInformation> dao
            = new HashLookupDao<PluginRefInformation>(PluginRefInformation.class);

    /**
     * The Name.
     */
    private String name;

    /**
     * The list of plugin ref values
     */
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
    private List<PluginRefValue> values;

    /**
     * The Values json.
     */
    @Transient
    @JsonProperty("values")
    private RefValues valuesJson;

    /**
     * The Url.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="url_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Url url;

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

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets values.
     *
     * @return the values
     */
    public List<PluginRefValue> getValues() {
        return this.values;
    }

    /**
     * Sets values.
     *
     * @param values the values
     */
    public void setValues(List<PluginRefValue> values) {
        if (this.values == values) return;
        this.values = values;
        if (this.valuesJson != null) this.valuesJson.setValue(this.values);
    }

    /**
     * Gets values json.
     *
     * @return the values json
     */
    public RefValues getValuesJson() {
        if (this.valuesJson == null) {
            this.valuesJson = new RefValues();
            this.valuesJson.takeFieldsFromParent(this);
        }
        return this.valuesJson;
    }

    /**
     * Sets values json.
     *
     * @param values the values
     */
    public void setValuesJson(RefValues values) {
        if (this.valuesJson != null && this.valuesJson != values) {
            this.valuesJson.clearParent();
        }
        this.valuesJson = values;
        if (values != null) values.putFieldsIntoParent(this);
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url the url
     */
    public void setUrl(Url url) {
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
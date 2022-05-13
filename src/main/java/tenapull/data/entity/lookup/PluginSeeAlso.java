package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the plugin_see_also table
 */
@Entity(name = "PluginSeeAlso")
@Table(name = "plugin_see_also")
public class PluginSeeAlso extends SimpleStringLookupPojo<PluginSeeAlso> {
    public static final SimpleStringLookupDao<PluginSeeAlso> dao
            = new SimpleStringLookupDao<PluginSeeAlso>(PluginSeeAlso.class);

}
package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

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
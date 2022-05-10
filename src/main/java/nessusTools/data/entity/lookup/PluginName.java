package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the plugin_name table
 */
@Entity(name = "PluginName")
@Table(name = "plugin_name")
public class PluginName extends SimpleStringLookupPojo<PluginName> {
    public static final SimpleStringLookupDao<PluginName> dao
            = new SimpleStringLookupDao<PluginName>(PluginName.class);

}
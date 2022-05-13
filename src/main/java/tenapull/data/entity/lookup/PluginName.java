package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

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
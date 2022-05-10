package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the plugin_family table
 */
@Entity(name = "PluginFamily")
@Table(name = "plugin_family")
public class PluginFamily extends SimpleStringLookupPojo<PluginFamily> {
    public static final SimpleStringLookupDao<PluginFamily> dao
            = new SimpleStringLookupDao<PluginFamily>(PluginFamily.class);

}
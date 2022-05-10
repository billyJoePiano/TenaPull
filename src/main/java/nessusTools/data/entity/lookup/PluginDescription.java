package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the plugin_description lookup table,
 * that is indexed by a SHA-512 hash
 */
@Entity(name = "PluginDescription")
@Table(name = "plugin_description")
public class PluginDescription extends StringHashLookupPojo<PluginDescription> {
    public static final StringHashLookupDao<PluginDescription> dao
            = new StringHashLookupDao<PluginDescription>(PluginDescription.class);

}
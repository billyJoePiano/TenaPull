package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the plugin_synopsis lookup table,
 * that is indexed by a SHA-512 hash
 */
@Entity(name = "PluginSynopsis")
@Table(name = "plugin_synopsis")
public class PluginSynopsis extends StringHashLookupPojo<PluginSynopsis> {
    public static final StringHashLookupDao<PluginSynopsis> dao
            = new StringHashLookupDao<PluginSynopsis>(PluginSynopsis.class);

}
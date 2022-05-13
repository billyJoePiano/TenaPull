package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the plugin_solution lookup table,
 * that is indexed by a SHA-512 hash
 */
@Entity(name = "PluginSolution")
@Table(name = "plugin_solution")
public class PluginSolution extends StringHashLookupPojo<PluginSolution> {
    public static final StringHashLookupDao<PluginSolution> dao
            = new StringHashLookupDao<PluginSolution>(PluginSolution.class);

}
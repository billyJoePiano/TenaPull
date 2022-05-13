package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the plugin_description lookup table,
 * that is indexed by a SHA-512 hash
 */
@Entity(name = "PluginScriptCopyright")
@Table(name = "plugin_script_copyright")
public class PluginScriptCopyright extends StringHashLookupPojo<PluginScriptCopyright> {
    public static final StringHashLookupDao<PluginScriptCopyright> dao
            = new StringHashLookupDao<PluginScriptCopyright>(PluginScriptCopyright.class);

}
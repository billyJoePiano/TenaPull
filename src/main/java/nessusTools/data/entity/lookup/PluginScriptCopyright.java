package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginScriptCopyright")
@Table(name = "plugin_script_copyright")
public class PluginScriptCopyright extends StringHashLookupPojo<PluginScriptCopyright> {
    public static final StringHashLookupDao<PluginScriptCopyright> dao
            = new StringHashLookupDao<PluginScriptCopyright>(PluginScriptCopyright.class);

}
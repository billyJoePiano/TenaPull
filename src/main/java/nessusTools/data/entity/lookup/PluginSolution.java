package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginSolution")
@Table(name = "plugin_solution")
public class PluginSolution extends StringHashLookupPojo<PluginSolution> {
    public static final StringHashLookupDao<PluginSolution> dao
            = new StringHashLookupDao<PluginSolution>(PluginSolution.class);

}
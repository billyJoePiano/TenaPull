package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginFamily")
@Table(name = "plugin_family")
public class PluginFamily extends SimpleStringLookupPojo<PluginFamily> {
    public static final SimpleStringLookupDao<PluginFamily> dao
            = new SimpleStringLookupDao<PluginFamily>(PluginFamily.class);

}
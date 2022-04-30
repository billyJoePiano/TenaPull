package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginRefValue")
@Table(name = "plugin_ref_value")
public class PluginRefValue extends SimpleStringLookupPojo<PluginRefValue> {
    public static final SimpleStringLookupDao<PluginRefValue> dao
            = new SimpleStringLookupDao<PluginRefValue>(PluginRefValue.class);

}
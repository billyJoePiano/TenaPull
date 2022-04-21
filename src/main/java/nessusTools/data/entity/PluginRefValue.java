package nessusTools.data.entity;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginRefValue")
@Table(name = "plugin_ref_value")
public class PluginRefValue extends LookupPojo<PluginRefValue> {
    public static final LookupDao<PluginRefValue> dao
            = new LookupDao<PluginRefValue>(PluginRefValue.class);

}
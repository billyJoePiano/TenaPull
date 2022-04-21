package nessusTools.data.entity;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginSeeAlso")
@Table(name = "plugin_see_also")
public class PluginSeeAlso extends LookupPojo<PluginSeeAlso> {
    public static final LookupDao<PluginSeeAlso> dao
            = new LookupDao<PluginSeeAlso>(PluginSeeAlso.class);

}
package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the plugin_risk_factor table
 */
@Entity(name = "PluginRiskFactor")
@Table(name = "plugin_risk_factor")
public class PluginRiskFactor extends SimpleStringLookupPojo<PluginRiskFactor> {
    public static final SimpleStringLookupDao<PluginRiskFactor> dao
            = new SimpleStringLookupDao<PluginRiskFactor>(PluginRiskFactor.class);

}
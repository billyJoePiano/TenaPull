package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the policy_template_uuid table
 */
@Entity(name = "PolicyTemplateUuid")
@Table(name = "policy_template_uuid")
public class PolicyTemplateUuid extends SimpleStringLookupPojo<PolicyTemplateUuid> {
    public static final SimpleStringLookupDao<PolicyTemplateUuid> dao
            = new SimpleStringLookupDao<>(PolicyTemplateUuid.class);
}

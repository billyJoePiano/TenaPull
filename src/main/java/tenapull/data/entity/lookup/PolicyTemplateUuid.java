package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

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

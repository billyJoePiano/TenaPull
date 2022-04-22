package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PolicyTemplateUuid")
@Table(name = "policy_template_uuid")
public class PolicyTemplateUuid extends LookupPojo<PolicyTemplateUuid> {
    public static final LookupDao<PolicyTemplateUuid> dao
            = new LookupDao<>(PolicyTemplateUuid.class);
}

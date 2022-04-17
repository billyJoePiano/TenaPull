package nessusTools.data.entity.template;

public interface ObjectLookupPojo<OL extends ObjectLookupPojo> extends DbPojo {
    public void _set(OL objectLookup);
}

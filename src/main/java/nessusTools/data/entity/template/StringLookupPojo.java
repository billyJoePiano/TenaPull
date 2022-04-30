package nessusTools.data.entity.template;

public interface StringLookupPojo<POJO extends StringLookupPojo<POJO>>
        extends DbPojo, Comparable<POJO> {

    String getValue();
    void setValue(String value);
}

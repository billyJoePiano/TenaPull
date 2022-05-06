package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "License")
@Table(name = "license")
public class License extends GeneratedIdPojo implements MapLookupPojo<License> {

	public static final MapLookupDao<License> dao
			= new MapLookupDao<License>(License.class);

	@Column(name = "`limit`") // tick marks to escape the field name, because 'limit' is a SQL keyword
    @Convert(converter = MultiTypeWrapper.Converter.class)
    @JsonDeserialize(using = MultiType.Deserializer.class)
    @JsonSerialize(using = MultiType.Serializer.class)
	private MultiTypeWrapper limit;

    @Column
    @Convert(converter = MultiTypeWrapper.Converter.class)
    @JsonDeserialize(using = MultiType.Deserializer.class)
    @JsonSerialize(using = MultiType.Serializer.class)
	private MultiTypeWrapper trimmed;

	public MultiTypeWrapper getLimit(){
		return this.limit;
	}

	public MultiTypeWrapper getTrimmed(){
		return this.trimmed;
	}

    public void setLimit(MultiTypeWrapper limit) {
        this.limit = limit;
    }

    public void setTrimmed(MultiTypeWrapper trimmed) {
        this.trimmed = trimmed;
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(License o) {
        this.__set(o);
        this.limit = o.limit;
        this.trimmed = o.trimmed;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(License o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.limit, o.limit)
                && Objects.equals(this.trimmed, o.trimmed)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }


    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "limit", this.limit,
                "trimmed", this.trimmed,
                "extraJson", this.getExtraJson()
        });
    }
}

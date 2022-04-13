package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanPlugin")
@Table(name = "scan_plugin")
public class ScanPlugin extends ScanResponse.ChildListTemplate {
    Integer hostCount;

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_plugin_scan_host",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "acl_id") }
    )
    @OrderColumn(name = "host_id")
    @JsonDeserialize(contentAs = Host.class, contentUsing = ObjectLookup.Deserializer.class)
    List<Host> hosts;


    @Entity(name = "ScanPlugin.Host")
    @Table(name = "scan_plugin_host")
    @JsonIgnoreProperties({"id"})
    public static class Host extends ScanResponse.ChildListTemplate {
        public static final Dao<Host> dao = new ObjectLookupDao(Host.class);

        // TODO ManyToMany and ObjectLookup logic

        @Column(name = "host_id")
        @JsonProperty("id")
        Integer hostId;

        @Column(name = "host_fqdn")
        @JsonProperty("host_fqdn")
        String hostFqdn;

        String hostname;

        public Integer getHostId() {
            return hostId;
        }

        public void setHostId(Integer hostId) {
            this.hostId = hostId;
        }

        public String getHostFqdn() {
            return hostFqdn;
        }

        public void setHostFqdn(String hostFqdn) {
            this.hostFqdn = hostFqdn;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }


    }
}

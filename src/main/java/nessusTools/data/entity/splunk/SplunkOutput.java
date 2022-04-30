package nessusTools.data.entity.splunk;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.host.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.*;
import java.util.*;

@Entity(name = "SplunkOutput")
@Table(name = "splunk_output")
public class SplunkOutput implements DbPojo {
    public static final Dao<SplunkOutput> dao = new Dao<>(SplunkOutput.class);

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    @JsonIgnore
    private int id;

    @CreationTimestamp
    private Timestamp timestamp;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    @JsonIgnore
    private ScanHostResponse hostResponse;

    @Transient
    private final ScanHostSummary host = new ScanHostSummary();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "vulnerability_id")
    private Vulnerability vulnerability;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "scan_plugin_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ScanPlugin plugin;

    @Transient
    @JsonProperty("plugin_error")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPluginError() {
        if (this.plugin != null) return null;
        if (this.pluginBestGuess != null) {
            return "NessusTools was unable find a matching Plugin in its cached list of plugins used by this scan, "
                    + "but found another plugin that matches the vulnerability description";
        } else {
            return "NessusTools was unable find a matching Plugin in its cached list of plugins used by this scan, "
                    + "and could not locate another plugin that matches the vulnerability description";
        }
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "plugin_best_guess_id")
    @JsonProperty("plugin (NessusTools best guess)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Plugin pluginBestGuess;

    @Transient
    private final ScanSummary scan = new ScanSummary();

    public SplunkOutput() { }

    public SplunkOutput(ScanHostResponse hostResponse, Vulnerability vulnerability) {
        this.hostResponse = hostResponse;
        this.vulnerability = vulnerability;
        this.getPlugin();
    }

    public void setPlugin(ScanPlugin plugin) {
        this.plugin = plugin;

    }

    @Override
    public void _prepare() {
        if (this.hostResponse != null) {
            this.hostResponse._prepare();
        }

        this.vulnerability = Vulnerability.dao.getOrCreate(this.vulnerability);
        this.plugin = ScanPlugin.dao.getOrCreate(this.plugin);
        this.pluginBestGuess = Plugin.dao.getOrCreate(this.pluginBestGuess);
    }

    public ScanPlugin getPlugin() {
        if (this.plugin != null) return this.plugin;

        if (hostResponse == null || vulnerability == null) {
            return null;
        }

        Integer pluginId = vulnerability.getPluginId();
        String pluginIdStr = pluginId != null ? pluginId.toString() : null;
        PluginName name = this.vulnerability.getPluginName();

        ScanResponse scanResponse = hostResponse.getScanResponse();

        if (scanResponse == null) {
            this.pluginBestGuess = findAltPlugin(pluginId, pluginIdStr, name);
            return this.plugin;
        }

        Map<ScanPlugin, Integer> candidates = new LinkedHashMap<>();
        int highest = 0;

        List<ScanPlugin> plugins = scanResponse.getPlugins();
        if (plugins != null) {
            for (ScanPlugin plugin : plugins) {
                if (plugin == null) continue;
                int rating = this.matchPlugin(plugin.getPlugin(), pluginId, pluginIdStr, name);
                if (rating >= 4) {
                    return this.plugin = plugin;

                } else if (rating > 0) {
                    candidates.put(plugin, rating);
                    if (rating > highest) {
                        highest = rating;
                    }
                }
            }
        }

        for (Map.Entry<ScanPlugin, Integer> entry : candidates.entrySet()) {
            if (entry.getValue() == highest) {
                return this.plugin = entry.getKey();
            }
        }
        this.pluginBestGuess = findAltPlugin(pluginId, pluginIdStr, name);
        return this.plugin;
    }

    private static Plugin findAltPlugin(Integer pluginId,
                              String pluginIdStr,
                              PluginName name) {

        Map<Plugin, Integer> candidates = new LinkedHashMap<>();
        Set<PluginAttributes> checked = new LinkedHashSet<>();
        Var.Int highest = new Var.Int(0);

        if (name != null) {
            List<PluginAttributes> attrs
                    = PluginAttributes.dao.findByPropertyEqual("pluginName", name);

            Plugin result = matchAttributes(attrs, candidates, checked,
                                    highest, pluginId, pluginIdStr, name);

            if (result != null) return result;
        }

        if (pluginIdStr != null) {
            List<Plugin> plgs = Plugin.dao.findByPropertyEqual("pluginId", pluginIdStr);
            for (Plugin plugin : plgs) {
                if (plugin == null) continue;
                if (candidates.containsKey(plugin)) continue;
                int rating = matchPlugin(plugin, pluginId, pluginIdStr, name);
                if (rating >= 4) {
                    return plugin;

                } else if (rating > 0) {
                    candidates.put(plugin, rating);
                    if (rating > highest.value) {
                        highest.value = rating;
                    }
                }
            }
        }

        if (pluginId != null) {
            List<PluginInformation> infos
                    = PluginInformation.dao.findByPropertyEqual("pluginId", pluginId);

            for (PluginInformation info : infos) {
                if (info == null) continue;
                List<PluginAttributes> attrs
                        = PluginAttributes.dao.findByPropertyEqual("pluginInformation", name);

                Plugin result = matchAttributes(attrs, candidates, checked,
                        highest, pluginId, pluginIdStr, name);

                if (result != null) return result;
            }
        }

        for (Map.Entry<Plugin, Integer> entry : candidates.entrySet()) {
            if (entry.getValue() == highest.value) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static Plugin matchAttributes(List<PluginAttributes> attrs,
                                        Map<Plugin, Integer> candidates,
                                        Set<PluginAttributes> checked,
                                        Var.Int highest,
                                        Integer pluginId,
                                        String pluginIdStr,
                                        PluginName name) {

        for (PluginAttributes attr : attrs) {
            if (attr == null) continue;
            if (!checked.add(attr)) continue;
            List<Plugin> plugins = Plugin.dao.findByPropertyEqual("attributes", attr);
            for (Plugin plugin :plugins) {
                if (plugin == null) continue;
                if (candidates.containsKey(plugin)) continue;
                int rating = matchPlugin(plugin, pluginId, pluginIdStr, name);
                if (rating >= 4) {
                    return plugin;

                } else if (rating > 0) {
                    candidates.put(plugin, rating);
                    if (rating > highest.value) {
                        highest.value = rating;
                    }
                }
            }
        }
        return null;
    }

    private static int matchPlugin(Plugin plugin,
                                Integer pluginId,
                                String pluginIdStr,
                                PluginName name) {

        if (plugin == null) return 0;
        int rating = 0;

        if (pluginIdStr != null && Objects.equals(pluginIdStr, plugin.getPluginId())) {
            rating++;
        }


        if (name != null && Objects.equals(name, plugin.getPluginName())) {
            rating++;
        }

        PluginAttributes attributes = plugin.getPluginAttributes();
        if (attributes == null) return rating;

        PluginInformation info = attributes.getPluginInformation();

        if (info != null && pluginId != null
                && Objects.equals(pluginId, info.getPluginId())) {

            rating++;
        }

        if (name != null && Objects.equals(name, attributes.getPluginName())) {
            rating++;
        }
        return rating;
    }


    @JsonSerialize(using = SummarySerializer.class)
    public class ScanSummary implements SummarySerializer.Summary<Scan, ScanInfo> {
        private ScanSummary() { }

        Integer id;

        @Override
        public String getName() {
            return "scan";
        }

        @Override
        public Integer getId() {
            return this.id;
        }

        private ScanResponse getScanResponse() {
            if (SplunkOutput.this.hostResponse == null) return null;
            ScanResponse sr = SplunkOutput.this.hostResponse.getScanResponse();
            if (sr != null) {
                this.id = sr.getId();
            }
            return sr;
        }

        @Override
        public Scan getSummary() {
            ScanResponse scanResponse = this.getScanResponse();
            if (scanResponse == null) return null;
            Scan scan = scanResponse.getScan();
            if (scan == null) {
                scan = Scan.dao.getById(scanResponse.getId());
                if (scan != null) {
                    scanResponse.setScan(scan);
                }
            }
            return scan;
        }

        @Override
        public ScanInfo getDetails() {
            ScanResponse scanResponse = this.getScanResponse();
            if (scanResponse == null) return null;
            ScanInfo info = scanResponse.getInfo();
            if (info == null) {
                info = ScanInfo.dao.getById(scanResponse.getId());
                if (info != null) {
                    scanResponse.setInfo(info);
                }
            }
            return info;
        }
    }

    @JsonSerialize(using = SummarySerializer.class)
    public class ScanHostSummary implements SummarySerializer.Summary<ScanHostInfo, ScanHost> {
        private ScanHostSummary() { }

        Integer id;

        @Override
        public String getName() {
            return "host";
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public ScanHostInfo getSummary() {
            if (SplunkOutput.this.hostResponse == null) return null;
            this.id = SplunkOutput.this.hostResponse.getId();

            ScanHostInfo info = SplunkOutput.this.hostResponse.getInfo();
            if (info == null) {
                info = ScanHostInfo.dao.getById(SplunkOutput.this.hostResponse.getId());
                if (info != null) {
                    SplunkOutput.this.hostResponse.setInfo(info);
                }
            }
            return info;
        }

        @Override
        public ScanHost getDetails() {
            if (SplunkOutput.this.hostResponse == null) return null;
            this.id = SplunkOutput.this.hostResponse.getId();

            ScanHost host = SplunkOutput.this.hostResponse.getHost();
            if (host == null) {
                host = ScanHost.dao.getById(SplunkOutput.this.hostResponse.getId());
                if (host != null) {
                    SplunkOutput.this.hostResponse.setHost(host);
                }
            }
            return host;
        }
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp generated) {
        this.timestamp = generated;
    }

    public ScanHostResponse getHostResponse() {
        return hostResponse;
    }

    public void setHostResponse(ScanHostResponse hostResponse) {
        this.hostResponse = hostResponse;
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Vulnerability vulnerability) {
        this.vulnerability = vulnerability;
    }

    public ScanSummary getScan() {
        return scan;
    }

    public ScanHostSummary getHost() {
        return host;
    }

    public ObjectNode toJsonNode() {
        return new SplunkOutputSerializer().valueToTree (this);
    }

    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String toString() {
        try {
            return this.toJsonString();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for '"
                    + super.toString() + "' :\n"
                    + e.getMessage();
        }
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(o.getClass(), this.getClass())) return false;

        SplunkOutput other = (SplunkOutput) o;
        return  (this.getId() == 0 || other.getId() == 0 || this.getId() == other.getId())
                && Objects.equals(this.toJsonNode(), other.toJsonNode());

    }
}

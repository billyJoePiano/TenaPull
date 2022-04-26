package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginVulnInformation")
@Table(name = "plugin_vuln_information")
public class PluginVulnInformation extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginVulnInformation> {

    public static final ObjectLookupDao<PluginVulnInformation> dao
            = new ObjectLookupDao<PluginVulnInformation>(PluginVulnInformation.class);

    @Column(name = "exploitability_ease")
    @JsonProperty("exploitability_ease")
    private String exploitabilityEase;

    @Column(name = "in_the_news")
    @JsonProperty("in_the_news")
    String inTheNews;

    @Column(name = "exploit_available")
    @JsonProperty("exploit_available")
    private String exploitAvailable;

    @Column(name = "vuln_publication_date")
    @JsonProperty("vuln_publication_date")
    String vulnPublicationDate;

    @Column(name = "patch_publication_date")
    @JsonProperty("patch_publication_date")
    private String patchPublicationDate;

    @Override
    public void _set(PluginVulnInformation o) {
        this.__set(o);
        this.exploitabilityEase = o.exploitabilityEase;
        this.inTheNews = o.inTheNews;
        this.exploitAvailable = o.exploitAvailable;
        this.vulnPublicationDate = o.vulnPublicationDate;
        this.patchPublicationDate = o.patchPublicationDate;
    }

    public String getInTheNews() {
        return inTheNews;
    }

    public void setInTheNews(String inTheNews) {
        this.inTheNews = inTheNews;
    }

    public String getVulnPublicationDate() {
        return vulnPublicationDate;
    }

    public void setVulnPublicationDate(String vulnPublicationDate) {
        this.vulnPublicationDate = vulnPublicationDate;
    }

    public String getExploitabilityEase() {
        return exploitabilityEase;
    }

    public void setExploitabilityEase(String exploitabilityEase) {
        this.exploitabilityEase = exploitabilityEase;
    }

    public String getExploitAvailable() {
        return exploitAvailable;
    }

    public void setExploitAvailable(String exploitAvailable) {
        this.exploitAvailable = exploitAvailable;
    }

    public String getPatchPublicationDate() {
        return patchPublicationDate;
    }

    public void setPatchPublicationDate(String patchPublicationDate) {
        this.patchPublicationDate = patchPublicationDate;
    }
}

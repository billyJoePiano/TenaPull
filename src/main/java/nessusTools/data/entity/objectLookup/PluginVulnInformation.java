package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a reusable "object lookup", for the vuln_information object included
 * in the plugin attributes returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "PluginVulnInformation")
@Table(name = "plugin_vuln_information")
public class PluginVulnInformation extends HashLookupTemplate<PluginVulnInformation> {

    /**
     * The dao for PluginVulnInformation
     */
    public static final HashLookupDao<PluginVulnInformation> dao
            = new HashLookupDao<PluginVulnInformation>(PluginVulnInformation.class);

    @Column(name = "exploitability_ease")
    @JsonProperty("exploitability_ease")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String exploitabilityEase;

    @Column(name = "in_the_news")
    @JsonProperty("in_the_news")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String inTheNews;

    @Column(name = "exploit_available")
    @JsonProperty("exploit_available")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String exploitAvailable;

    @Column(name = "vuln_publication_date")
    @JsonProperty("vuln_publication_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vulnPublicationDate;

    @Column(name = "patch_publication_date")
    @JsonProperty("patch_publication_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String patchPublicationDate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cpe_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Cpe cpe;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(PluginVulnInformation o) {
        this.__set(o);
        this.exploitabilityEase = o.exploitabilityEase;
        this.inTheNews = o.inTheNews;
        this.exploitAvailable = o.exploitAvailable;
        this.vulnPublicationDate = o.vulnPublicationDate;
        this.patchPublicationDate = o.patchPublicationDate;
        this.cpe = o.cpe;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginVulnInformation o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.exploitabilityEase, o.exploitabilityEase)
                && Objects.equals(this.inTheNews, o.inTheNews)
                && Objects.equals(this.exploitAvailable, o.exploitAvailable)
                && Objects.equals(this.vulnPublicationDate, o.vulnPublicationDate)
                && Objects.equals(this.patchPublicationDate, o.patchPublicationDate)
                && Objects.equals(this.cpe, o.cpe)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    /**
     * Gets in the news.
     *
     * @return the in the news
     */
    public String getInTheNews() {
        return inTheNews;
    }

    /**
     * Sets in the news.
     *
     * @param inTheNews the in the news
     */
    public void setInTheNews(String inTheNews) {
        this.inTheNews = inTheNews;
    }

    /**
     * Gets vuln publication date.
     *
     * @return the vuln publication date
     */
    public String getVulnPublicationDate() {
        return vulnPublicationDate;
    }

    /**
     * Sets vuln publication date.
     *
     * @param vulnPublicationDate the vuln publication date
     */
    public void setVulnPublicationDate(String vulnPublicationDate) {
        this.vulnPublicationDate = vulnPublicationDate;
    }

    /**
     * Gets exploitability ease.
     *
     * @return the exploitability ease
     */
    public String getExploitabilityEase() {
        return exploitabilityEase;
    }

    /**
     * Sets exploitability ease.
     *
     * @param exploitabilityEase the exploitability ease
     */
    public void setExploitabilityEase(String exploitabilityEase) {
        this.exploitabilityEase = exploitabilityEase;
    }

    /**
     * Gets exploit available.
     *
     * @return the exploit available
     */
    public String getExploitAvailable() {
        return exploitAvailable;
    }

    /**
     * Sets exploit available.
     *
     * @param exploitAvailable the exploit available
     */
    public void setExploitAvailable(String exploitAvailable) {
        this.exploitAvailable = exploitAvailable;
    }

    /**
     * Gets patch publication date.
     *
     * @return the patch publication date
     */
    public String getPatchPublicationDate() {
        return patchPublicationDate;
    }

    /**
     * Sets patch publication date.
     *
     * @param patchPublicationDate the patch publication date
     */
    public void setPatchPublicationDate(String patchPublicationDate) {
        this.patchPublicationDate = patchPublicationDate;
    }

    /**
     * Gets cpe.
     *
     * @return the cpe
     */
    public Cpe getCpe() {
        return cpe;
    }

    /**
     * Sets cpe.
     *
     * @param cpe the cpe
     */
    public void setCpe(Cpe cpe) {
        this.cpe = cpe;
    }
}

package nessusTools.data.entity;

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

    @Column(name = "in_the_news")
    @JsonProperty("in_the_news")
    String inTheNews;

    @Column(name = "vuln_publication_date")
    @JsonProperty("vuln_publication_date")
    String vulnPublicationDate;


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

    @Override
    public void _set(PluginVulnInformation o) {
        this.setId(o.getId());
        //TODO
        this.setExtraJson(o.getExtraJson());
    }
}

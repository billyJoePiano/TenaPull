package tenapull.data.entity.scan;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import org.apache.logging.log4j.*;

/**
 * Represents a folder object returned by the Nessus API in /scans
 */
@Entity(name = "Folder")
@Table(name = "folder")
public class Folder extends NaturalIdPojo {

    /**
     * The dao for Folder
     */
    public static final Dao<Folder> dao = new Dao<Folder>(Folder.class);

    /**
     * The logger for Folder
     */
    public static final Logger logger = LogManager.getLogger(Folder.class);

    @Column
    private String name;

    @Column
    private String type;

    @Column(name = "default_tag")
    @JsonProperty("default_tag")
    private Integer defaultTag;

    @Column
    private Integer custom;

    @Column(name = "unread_count")
    @JsonProperty("unread_count")
    private Integer unreadCount;

    /*
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    @JsonIgnore
    private List<Scan> scans;
     */

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    /*
    @Override
    public void _set(Folder o) {
        this.__set(o);
        this.name = o.name;
        this.type = o.type;
        this.defaultTag = o.defaultTag;
        this.custom = o.custom;
        this.unreadCount = o.unreadCount;
        //this.scans = o.scans;
    }
     */


    /**
     * Instantiates a new Folder.
     */
    public Folder() { }

    /**
     * Instantiates a new Folder.
     *
     * @param id          the id
     * @param name        the name
     * @param type        the type
     * @param defaultTag  the default tag
     * @param custom      the custom
     * @param unreadCount the unread count
     */
    public Folder(int id,
                  String name,
                  String type,
                  Integer defaultTag,
                  Integer custom,
                  Integer unreadCount/*,
                  List<Scan> scans*/) {

        this.setId(id);
        this.name = name;
        this.type = type;
        this.defaultTag = defaultTag;
        this.custom = custom;
        this.unreadCount = unreadCount;
        //this.scans = scans;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets default tag.
     *
     * @return the default tag
     */
    public Integer getDefaultTag() {
        return defaultTag;
    }

    /**
     * Sets default tag.
     *
     * @param defaultTag the default tag
     */
    public void setDefaultTag(Integer defaultTag) {
        this.defaultTag = defaultTag;
    }

    /**
     * Gets custom.
     *
     * @return the custom
     */
    public Integer getCustom() {
        return custom;
    }

    /**
     * Sets custom.
     *
     * @param custom the custom
     */
    public void setCustom(Integer custom) {
        this.custom = custom;
    }

    /**
     * Gets unread count.
     *
     * @return the unread count
     */
    public Integer getUnreadCount() {
        return unreadCount;
    }

    /**
     * Sets unread count.
     *
     * @param unreadCount the unread count
     */
    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    /*
    public List<Scan> getScans() {
        return scans;
    }

    public void setScans(List<Scan> scans) {
        this.scans = scans;
    }
     */
}
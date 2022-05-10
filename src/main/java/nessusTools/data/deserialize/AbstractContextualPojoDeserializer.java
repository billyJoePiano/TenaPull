package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.DbPojo;
import nessusTools.data.persistence.Dao;
import org.apache.logging.log4j.Logger;


/**
 * Further implementation of AbstractContextualDeserializer that is specific to POJOs,
 * and which also obtains the appropriate Dao for the pojo type provided
 *
 * @param <POJO> the type parameter
 * @param <DAO>  the type parameter
 */
public abstract class AbstractContextualPojoDeserializer<POJO extends DbPojo, DAO extends Dao<POJO>>
        extends AbstractContextualDeserializer<POJO> {

    /**
     * The Pojo type.  This mirrors the type property in the parent superclass AbstractContextualDeserializer,
     * but is protected instead of private so that subclass implementations may access it directly.
     */
    protected Class<POJO> pojoType = null;
    /**
     * The Dao for the target pojo type
     */
    protected DAO dao = null;

    /**
     * First invokes the super's setType, then also sets its own pojoType and finds the appropriate Dao for the type
     * @param pojoType
     */
    public void setType(Class<POJO> pojoType) {
        super.setType(pojoType);
        fetchDao();
    }

    /**
     * Sets its own pojoType based on super.getType(), and finds the appropriate Dao for that type
     *
     * @return whether there was an error that should be logged by the invoker
     */
    private boolean fetchDao() {
        //return false if the deserializationContext and beanProperty should be logged, when available
        this.pojoType = this.getType();
        if (this.pojoType != null) {
            this.dao = Dao.get(pojoType);

            if (this.dao == null) {
                Logger logger = this.getLogger();
                logger.error("Could not find dao for raw class");
                logger.error(this.pojoType);
                logger.error(this.getJavaType());
                return false;
            }
        } else {
            // return true because the super class already logged this error
            this.dao = null;
        }
        return true;
    }


    /**
     * First invokes super.createContextual, which obtains the target deserialization type.  Then
     * obtains the type's Dao through calling the fetchDao() method
     *
     * @param deserializationContext
     * @param beanProperty
     * @return this, since it serves a dual-purpose as both the contextual parser and the deserializer
     * @throws JsonMappingException
     */
    @Override
    public JsonDeserializer<POJO> createContextual(
                                        DeserializationContext deserializationContext,
                                        BeanProperty beanProperty)
            throws JsonMappingException {

        super.createContextual(deserializationContext, beanProperty);

        if (!fetchDao()) {
            Logger logger = this.getLogger();
            logger.error(deserializationContext);
            logger.error(beanProperty);
        }

        return this;
    }
}

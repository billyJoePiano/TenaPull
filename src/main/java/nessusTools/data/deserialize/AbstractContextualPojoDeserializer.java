package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.DbPojo;
import nessusTools.data.persistence.Dao;
import org.apache.logging.log4j.Logger;

public abstract class AbstractContextualPojoDeserializer<POJO extends DbPojo, DAO extends Dao<POJO>>
        extends AbstractContextualDeserializer<POJO> {

    protected Class<POJO> pojoType = null;
    protected DAO dao = null;

    protected abstract Logger getLogger();

    public void setType(Class<POJO> pojoType) {
        super.setType(pojoType);
        fetchDao();

    }

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


    // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
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

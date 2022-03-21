package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.DbPojo;
import nessusTools.data.persistence.Dao;
import org.apache.logging.log4j.Logger;

public abstract class AbstractContextualPojoDeserializer<POJO extends DbPojo, DAO extends Dao<POJO>>
        extends AbstractContextualDeserializer<POJO> {

    protected Class<POJO> pojoClass = null;
    protected DAO dao = null;

    protected abstract Logger getLogger();


    // https://stackoverflow.com/questions/47348029/get-the-detected-generic-type-inside-jacksons-jsondeserializer
    @Override
    public JsonDeserializer<POJO> createContextual(
                                        DeserializationContext deserializationContext,
                                        BeanProperty beanProperty)
            throws JsonMappingException {

        super.createContextual(deserializationContext, beanProperty);

        this.pojoClass = this.getType();

         if (this.pojoClass != null) {
            this.dao = Dao.get(pojoClass);

            if (this.dao == null) {
                Logger logger = this.getLogger();
                logger.error("Could not find dao for raw class");
                logger.error(this.pojoClass);
                logger.error(this.getJavaType());
                logger.error(deserializationContext);
                logger.error(beanProperty);
            }
        }

        return this;
    }
}

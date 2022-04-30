package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.sync.*;
import org.hibernate.*;
import org.hibernate.proxy.*;

import java.util.*;

public abstract class AbstractPojoLookupDao<POJO extends DbPojo> extends Dao<POJO> {
    protected final InstancesTracker<Integer, POJO> instances;

    protected AbstractPojoLookupDao(Class<POJO> pojoType) {
        super(pojoType);
        this.instances = new InstancesTracker(Integer.class, pojoType, null);
    }

    public List<POJO> getOrCreate(List<POJO> list) {
        if (list == null) return null;

        List<POJO> newList = new ArrayList<>(list.size());
        for (POJO pojo : list) {
            newList.add(this.getOrCreate(pojo));
        }
        return newList;
    }

    public POJO getOrCreate(POJO pojo) {
        if (pojo == null) return null;

        int id = pojo.getId();
        if (id > 0) {
            POJO result = searchForInstanceId(pojo, id);
            if (result != null) {
                return finalizeResult(pojo, result);
            }
        }

        if (pojo instanceof HibernateProxy && !Hibernate.isInitialized(pojo)) {
            POJO result = this.unproxy(pojo);
            if (result == null) {
                return pojo;

            } else if (result instanceof HibernateProxy && !Hibernate.isInitialized(pojo)) {
                return result;
            }
            pojo = result;
        }
        pojo._prepare();
        return checkedGetOrCreate(pojo);
    }

    protected abstract POJO checkedGetOrCreate(POJO pojo);

    protected POJO searchForInstanceId(POJO pojo, int id) {
        return this.instances.getOrConstructWith(id, i -> {
            this.saveOrUpdate(pojo);
            return pojo;
        });
    }

    protected abstract POJO finalizeResult(POJO pojo, POJO result);
}

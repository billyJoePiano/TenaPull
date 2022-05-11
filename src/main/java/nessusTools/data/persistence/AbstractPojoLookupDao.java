package nessusTools.data.persistence;

import nessusTools.data.entity.template.*;
import nessusTools.sync.*;
import org.hibernate.*;
import org.hibernate.proxy.*;

import java.util.*;

/**
 * Abstract dao which implements some methods and functionality common to all lookup daos
 *
 * @param <POJO> the type parameter
 */
public abstract class AbstractPojoLookupDao<POJO extends DbPojo> extends Dao<POJO> {
    /**
     * An instances tracker mapping primary key ids to the "canonical" instance representing
     * that record.  This is needed because HIBERNATE WILL THROW EXCEPTIONS if the same record
     * is NOT represented by the same instance during an insert.  Since the same DB record is
     * OFTEN referenced multiple times in a single ScanResponse, it becomes necessary to keep
     * careful track of which instance is the "canonical" representation of which record,
     * to keep Hibernate happy.
     */
    protected final InstancesTracker<Integer, POJO> instances;

    /**
     * Instantiates a new Abstract pojo lookup dao.
     *
     * @param pojoType the pojo type
     */
    protected AbstractPojoLookupDao(Class<POJO> pojoType) {
        super(pojoType);
        this.instances = new InstancesTracker(Integer.class, pojoType, null);
    }

    /**
     * Get or create the "canonical" pojos representing each of the
     * the given pojos in the list
     *
     * @param list the list
     * @return the or create
     */
    public List<POJO> getOrCreate(List<POJO> list) {
        if (list == null) return null;

        List<POJO> newList = new ArrayList<>(list.size());
        for (POJO pojo : list) {
            newList.add(this.getOrCreate(pojo));
        }
        return newList;
    }

    /**
     * Get or create the "canonical" pojo representing the pojo provided.
     *
     * This is needed because HIBERNATE WILL THROW EXCEPTIONS if the same record
     * is NOT represented by the same instance during an insert.  Since the same DB record is
     * OFTEN referenced multiple times in a single ScanResponse, it becomes necessary to keep
     * careful track of which instance is the "canonical" representation of which record,
     * to keep Hibernate happy.
     *
     * @param pojo the pojo that needs to persisted, made the canonical instance,
     *             or replaced with the correct canonical instance
     * @return the canonical pojo to use for persisting a DB record
     */
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

    /**
     * Abstract method to be implemented by subclasses, which is invoked
     * after the instance array has already been checked for the instance
     * without success (assuming the pojo had a primary key id set) and the
     * pojo has already had its _prepare method invoked
     *
     * @param pojo the pojo
     * @return the pojo
     */
    protected abstract POJO checkedGetOrCreate(POJO pojo);


    /**
     * Search for pojo with instance id.  This is used by the initial
     * getOrCreate before invoking checkedGetOrCreate, and its behavior may
     * be overridden by subclass implementions if there are other criteria
     * to match (e.g. a natural key such as a hash or a map lookup)
     *
     * @param pojo the pojo
     * @param id   the primary key id
     * @return the pojo which matches the record
     */
    protected POJO searchForInstanceId(POJO pojo, int id) {
        return this.instances.getOrConstructWith(id, i -> {
            this.saveOrUpdate(pojo);
            return pojo;
        });
    }

    /**
     * Perform any needed final operations on the "canonical" pojo before returning
     * it to the caller of getOrCreate.  Typically, if the canonical pojo were a
     * different instance than the one provided, the _set method would be called on
     * the canonical instance to update it so it reflects the values of the new
     * instance
     *
     * @param pojo   the pojo
     * @param result the result
     * @return the pojo
     */
    protected abstract POJO finalizeResult(POJO pojo, POJO result);
}

package nessusTools.sync;

import java.lang.ref.*;
import java.util.*;

// TODO This needs a garbage collector, to remove defunct WeakRefs !!!!!!
public class WeakInstancesTracker<K, I> {
    private final Class<K> keyType;
    private final InstancesTracker<WeakInstancesTracker.WeakRef, I> tracker;
    //private final boolean keysComparable;
    private final Set<WeakRef> toDiscard = new LinkedHashSet<>();

    public WeakInstancesTracker(Class<K> keyType,
                                Class<I> instanceType,
                                Lambda1<K, I> constructLambda) {

        this.keyType = keyType;
        //this.keysComparable = Comparable.class.isAssignableFrom(keyType);

        this.tracker = new InstancesTracker<WeakInstancesTracker.WeakRef, I>(
                WeakInstancesTracker.WeakRef.class, instanceType, wr -> {

            K key = (K) wr.get();
            if (key != null) {
                return constructLambda.call(key);
            } else {
                throw new IllegalStateException(
                        "WeakRef was garbage collected while instance was under construction.");
            }
        });
    }

    /*
    public interface WR<K> {
        K get();
    }
     */

    public class WeakRef extends WeakReference<K>
            /*implements WR<K>*/ {

        private WeakRef(K referent) {
            super(referent);
        }
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (!this.getClass().equals(o.getClass())) {
                return false;
            }

            WeakRef other = (WeakRef) o;
            K myReferent = this.get();
            K otherReferent = other.get();

            if (myReferent == null || otherReferent == null) {
                return false;
            }
            return Objects.equals(myReferent, otherReferent);

        }
    }
    /*
    private int counter = 0;
    // to ensure that null dereferences can still be not equal and return something consistent
    // that is other than zero
    public class WeakRefComparable extends WeakRef
            implements WR<K>, Comparable {

        private int id;
        private WeakRefComparable(K referent) {
            super(referent);
            this.id = counter++;
        }

        @Override
        public int compareTo(Object o) throws NullPointerException {
            if (!o.getClass().equals(this.getClass())) {
                return -1;
            }

            if (o == this) return 0;

            WeakRefComparable other = (WeakRefComparable) o;
            Comparable myKey = (Comparable) this.get();
            Comparable otherKey = (Comparable) other.get();

            if (myKey == null) {
                if (otherKey == null) {
                    return this.id < other.id ? -1 : 1;
                } else {
                    return -1;
                }
            } else if (otherKey == null) {
                return 1;
            }

            return myKey.compareTo(otherKey);
        }
    }
     */

    private WeakRef make(K key) {
        /*if (keysComparable) {
            return new WeakRefComparable(key);

        } else {*/
            return new WeakRef(key);
        //}
    }

    public I get(K key) {
        return tracker.get(make(key));
    }

    public I getOrConstruct(K key) {
        return tracker.getOrConstruct(make(key));
    }

    public I getOrConstructWith(K key, Lambda1<K, I> lambda) {
        return tracker.getOrConstructWith(make(key), wr ->
            lambda.call(key)
        );
    }

    public I getOrConstructWith(K key, Lambda2<K, Lambda1<K, Void>, I> lambda) {
        Set<K>[] strongRefs = new Set[] { null };
        //to keep the alt keys from being garbage collected while the method is running

        return tracker.getOrConstructWith(make(key), (wr, akLambda) ->
            lambda.call(key, altKey -> {
                if (strongRefs[0] == null) {
                    strongRefs[0] = new LinkedHashSet<>();
                }
                WeakRef altWr = new WeakRef(altKey);
                strongRefs[0].add(altKey);
                return akLambda.call(altWr);
            })
        );
    }

    public I put(K key, I instance) {
        return tracker.put(make(key), instance);
    }

    public I constructWith(K key, Lambda1<K, I> customLambda) {
        return tracker.constructWith(make(key), wr -> customLambda.call(key));
    }

    public I remove (K key) {
        return tracker.remove(make(key));
    }

    public Set<K> getKeysFor(I instance) {
        return unwrapKeySet(tracker.getKeysFor(instance));
    }

    public Set<K> keySet() {
        return this.unwrapKeySet(tracker.keySet());
    }

    private Set<K> unwrapKeySet(Set wrKeys) {
        Set<K> keySet;
        /*if (this.keysComparable) {
            keySet = new TreeSet<>();

        } else {*/
        keySet = new LinkedHashSet<>();
        //}

        for (Object wro : wrKeys) {
            WeakRef wr = (WeakRef) wro;
            K key = wr.get();
            if (key != null) {
                keySet.add(key);
            } else {
                toDiscard.add(wr);
            }
        }

        return keySet;
    }

    public I read(K key, Lambda1<I, I> readLambda) {
        return tracker.read(make(key), readLambda);
    }

    public I write(Lambda1<Lambda2<K, I, I>, I> writeLambda) {
        return tracker.write(putWr -> writeLambda.call(
                (key, value) -> putWr.call(make(key), value)
            ));
    }
}

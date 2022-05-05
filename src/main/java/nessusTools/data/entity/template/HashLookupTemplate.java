package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.persistence.*;
import nessusTools.sync.*;
import nessusTools.util.Type;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import java.io.*;
import java.util.*;

@MappedSuperclass
public abstract class HashLookupTemplate<POJO extends HashLookupTemplate<POJO>>
        extends GeneratedIdPojo implements HashLookupPojo<POJO> {

    @NaturalId
    @Access(AccessType.PROPERTY)
    @Column(name = "_hash")
    @JsonIgnore
    @Convert(converter = Hash.Converter.class)
    private Hash _hash;

    @JsonIgnore
    @Override
    public Hash get_hash() {
        if (this._hash == null) {
            this.cachedNode = this.getOrCreateNodeCache();
        }
        return this._hash;
    }

    @JsonIgnore
    @Override
    public void set_hash(Hash hash) throws IllegalStateException {
        if (this._hash != null && !Objects.equals(this._hash, hash)) {
            throw new IllegalStateException("Cannot alter the hash of a HashLookup (" +
                    this.getClass() +") after it has been set!");
        }
        this._hash = hash;
    }

    @Transient
    @Override
    protected void __set(GeneratedIdPojo other) {
        super.__set(other);
        this.set_hash(((HashLookupTemplate)other)._hash);
    }

    @Transient
    @JsonIgnore
    public boolean _isHashCalculated() {
        return this._hash != null;
    }

    public void __prepare() {
        super.__prepare();
        if (this._hash == null) this.get_hash();
    }

    @Override
    public final ObjectNode toJsonNode() {
        if (this._hash == null) return super.toJsonNode();
        else return this.getOrCreateNodeCache().getNode();
    }

    @Override
    public final String toJsonString() throws JsonProcessingException {
        if (this._hash == null) return super.toJsonString();
        else return this.getOrCreateNodeCache().getString();
    }


    private static ReadWriteLock<Map<Class<? extends HashLookupTemplate>, NodeCache>, NodeCache>
            nodeCaches = ReadWriteLock.forMap(new LinkedHashMap<>());


    private static class NodeCache<POJO extends HashLookupTemplate<POJO>> {
        private final InstancesTracker<Hash, MainCachedNode> hash;

        private final InstancesTracker<Integer, MainCachedNode> id;

        private NodeCache(Class<POJO> pojoType) {
            hash = new InstancesTracker<>(
                        new Type<>(Hash.class),
                        new Type<>(MainCachedNode.class, pojoType),
                        null);

            id = new InstancesTracker<>(
                        new Type<>(Integer.class),
                        new Type<>(MainCachedNode.class, pojoType),
                        null);
        }
    }

    private static class CachedNode<NODE extends JsonNode> {
        private final NODE node;
        private final String string;

        private CachedNode(NODE node, String string) {
            this.node = node;
            this.string = string;
        }

        protected NODE getNode() {
            return this.node;
        }

        protected String getString() {
            return this.string;
        }
    }

    private static class MainCachedNode extends CachedNode<ObjectNode> {
        private Integer id;
        private final Hash hash;
        private final Map<Class<? extends ObjectMapper>, CachedNode>
                alt = new LinkedHashMap<>();

        private MainCachedNode(ObjectNode node, String string, Hash hash) {
            super(node, string);
            this.hash = hash;
        }

        private MainCachedNode(ObjectNode node, String string, Hash hash, int id) {
            this(node, string, hash);
            this.id = id;
        }
    }



    private NodeCache<POJO> getNodeCache() {
        Class<POJO> type = (Class<POJO>)this.getClass();
        NodeCache<POJO> cache = nodeCaches.read(nc -> nc.get(type));
        if (cache != null) return cache;
        return nodeCaches.write(nc -> {
            NodeCache<POJO> cm = nc.get(type);
            if (cm == null) {
                cm = new NodeCache<>(type);
                nc.put(type, cm);
            }
            return cm;
        });
    }

    @Transient
    @JsonIgnore
    private MainCachedNode cachedNode;

    private MainCachedNode getOrCreateNodeCache() {
        if (this.cachedNode != null) return this.cachedNode;
                //avoid the need for synchronization if it is already created
        synchronized (this) {
            this.cachedNode = lookForCachedNode();
        }

        if (this.cachedNode != null) return this.cachedNode;

        //construct OUTSIDE of the synchronized block.  Could cause deadlock
        ObjectNode node = new ObjectMapper().valueToTree(this);

        synchronized (this) {
            return this.cachedNode = putCachedNode(node);
        }
    }

    private synchronized MainCachedNode lookForCachedNode() {
        if (this.cachedNode != null) return this.cachedNode;
        // double-check, in case it was constructed while waiting for the lock

        NodeCache<POJO> cache = this.getNodeCache();
        MainCachedNode result = null;
        int id = this.getId();

        if (id != 0) {
            assert this._hash != null;

            result = cache.id.getOrConstructWith(id, i -> {
                MainCachedNode r = cache.hash.get(this._hash);
                if (r != null) {
                    synchronized (r) {
                        if (r.id == null) {
                            r.id = i;
                        } else {
                            assert r.id == id;
                        }
                    }
                }
                return r;
            });
            if (result != null) {
                assert Objects.equals(this._hash, result.hash);
            }

        } else if (this._hash != null) {
            result = cache.hash.get(this._hash);
        }
        return result;
    }


    private synchronized MainCachedNode putCachedNode(ObjectNode node) {
        if (this.cachedNode != null) return this.cachedNode;

        NodeCache<POJO> cache = this.getNodeCache();
        int id = this.getId();
        String string = node.toString();

        if (this._hash == null) {
            this._hash = new Hash(string);

        } else {
            assert Objects.equals(this._hash, new Hash(string));
        }

        if (id == 0) {
            return cache.hash.getOrConstructWith(this._hash,
                    h -> new MainCachedNode(node, string, this._hash));

        } else {
            return cache.id.getOrConstructWith(id,
                    i -> cache.hash.getOrConstructWith(this._hash,
                            h -> new MainCachedNode(node, string, this._hash, id)));
        }
    }


    public static <POJO extends HashLookupTemplate<POJO>> CachedNodeSerializer<POJO>
            getCachingSerializer(JsonSerializer<POJO> defaultSerializer, ObjectMapper mapper) {

        if (defaultSerializer instanceof ResolvableSerializer) {
            return new ResolvableCachedNodeSerializer<>(defaultSerializer, mapper);

        } else {
            return new CachedNodeSerializer<>(defaultSerializer, mapper);
        }
    }


    public static class CachedNodeSerializer<POJO extends HashLookupTemplate<POJO>>
            extends JsonSerializer<POJO> {

        protected final JsonSerializer<POJO> defaultSerializer;
        private final ObjectMapper mapper;
        private final Class<? extends ObjectMapper> mapperType;
        private final boolean generic;
        private final List<POJO> constructing;

        private CachedNodeSerializer(JsonSerializer<POJO> defaultSerializer,
                                     ObjectMapper mapper) {

            if (defaultSerializer == null) throw new NullPointerException();

            this.defaultSerializer = defaultSerializer;
            this.mapper = mapper;
            this.mapperType = mapper.getClass();
            this.generic = Objects.equals(this.mapperType, CachingMapper.class);
            if (generic) {
                this.constructing = null;

            } else {
                this.constructing = new LinkedList<>();
            }
        }

        @Override
        public void serialize(POJO value, JsonGenerator jg, SerializerProvider sp) throws IOException {
            if (value == null) {
                jg.writeNull(); //???
                return;
            }

            HashLookupTemplate<POJO> pojo = value;
            MainCachedNode mainCache = pojo.getOrCreateNodeCache();

            assert mapper == jg.getCodec();

            if (this.generic) {
                jg.writeTree(mainCache.getNode());
                return;
            }

            CachedNode alt;

            synchronized (mainCache.alt) {
                alt = mainCache.alt.get(mapperType);
                if (alt == null) {
                    if (this.constructing.contains(value)) {
                        this.defaultSerializer.serialize(value, jg, sp);
                        return;
                    }

                    this.setConstructingNode(value);
                    JsonNode node = mapper.valueToTree(value);
                    this.doneConstructingNode(value);

                    String string = node.toString();
                    alt = new CachedNode(node, string);
                    mainCache.alt.put(mapperType, alt);
                }
            }

            jg.writeTree(alt.node);
        }

        protected void setConstructingNode(POJO forValue) {
            constructing.add(forValue);
        }

        protected void doneConstructingNode(POJO forValue) {
            POJO type = this.constructing.remove(this.constructing.size() - 1);
            assert Objects.equals(type, forValue);
        }
    }

    public static class ResolvableCachedNodeSerializer<POJO extends HashLookupTemplate<POJO>>
            extends CachedNodeSerializer<POJO> implements ResolvableSerializer {

        private ResolvableCachedNodeSerializer(JsonSerializer<POJO> defaultSerializer,
                                     ObjectMapper mapper) {
            super(defaultSerializer, mapper);
        }

        @Override
        public void resolve(SerializerProvider provider) throws JsonMappingException {
            ((ResolvableSerializer)this.defaultSerializer).resolve(provider);
        }
    }
}

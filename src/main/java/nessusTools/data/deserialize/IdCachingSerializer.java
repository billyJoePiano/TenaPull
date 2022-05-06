package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import nessusTools.data.entity.template.*;
import nessusTools.sync.*;
import nessusTools.util.*;

import java.io.*;
import java.lang.ref.*;
import java.util.*;

public class IdCachingSerializer<POJO extends IdCachingSerializer.NodeCacher<POJO>>
        extends JsonSerializer<POJO> {


    public interface NodeCacher<POJO extends NodeCacher<POJO>> extends DbPojo {
        MainCachedNode<POJO> getCachedNode();
        void setCachedNode(MainCachedNode<POJO> node);
    }


    public static <POJO extends NodeCacher<POJO>> IdCachingSerializer<POJO>
            getCacheResetSerializer(JsonSerializer<POJO> defaultSerializer, ObjectMapper mapper) {

        if (defaultSerializer instanceof ResolvableSerializer) {
            return new ResolvableReset<>(defaultSerializer, mapper);

        } else {
            return new Reset<>(defaultSerializer, mapper);
        }
    }

    public static <POJO extends NodeCacher<POJO>> IdCachingSerializer<POJO>
            getIdCachingSerializer(JsonSerializer<POJO> defaultSerializer, ObjectMapper mapper) {

        if (defaultSerializer instanceof ResolvableSerializer) {
            return new Resolvable<>(defaultSerializer, mapper);

        } else {
            return new IdCachingSerializer<>(defaultSerializer, mapper);
        }
    }


    protected final JsonSerializer<POJO> defaultSerializer;
    protected final ObjectMapper mapper;
    protected final Class<? extends ObjectMapper> mapperType;
    protected final boolean generic;
    protected final List<POJO> constructing;

    private IdCachingSerializer(JsonSerializer<POJO> defaultSerializer,
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

        if (this.constructing.contains(value)) {
            this.defaultSerializer.serialize(value, jg, sp);
            return;
        }


        MainCachedNode mainCache = value.getCachedNode();
        if (mainCache == null) {
            if (value.getId() == 0) {
                this.defaultSerializer.serialize(value, jg, sp);
                return;
            }
            mainCache = getOrCreateNodeCache(value);
            value.setCachedNode(mainCache);
        }

        if (this.generic) {
            jg.writeTree(mainCache.getNode());
            return;
        }

        assert mapper == jg.getCodec();

        CachedNode alt;

        synchronized (mainCache.alt) {
            alt = (CachedNode) mainCache.alt.get(mapperType);
        }
        if (alt == null) {
            this.setConstructingNode(value);
            JsonNode node = mapper.valueToTree(value);
            this.doneConstructingNode(value);

            synchronized (mainCache.alt) {
                alt = (CachedNode)mainCache.alt.get(mapperType);
                if (alt == null) {
                    alt = new CachedNode(node);
                    mainCache.alt.put(mapperType, alt);
                }
            }
        }


        jg.writeTree(alt.node);
    }

    protected void setConstructingNode(POJO forValue) {
        constructing.add(forValue);
    }

    protected void doneConstructingNode(POJO forValue) {
        if (this.constructing.size() > 0) {
            POJO type = this.constructing.remove(this.constructing.size() - 1);
            assert Objects.equals(type, forValue);
        }
    }

    public static class Reset<POJO extends IdCachingSerializer.NodeCacher<POJO>>
            extends IdCachingSerializer<POJO> {

        private Reset(JsonSerializer<POJO> defaultSerializer,
                      ObjectMapper mapper) {

            super(defaultSerializer, mapper);
        }



        @Override
        public void serialize(POJO value, JsonGenerator jg, SerializerProvider sp) throws IOException {
            if (value == null) {
                jg.writeNull(); //???
                return;
            }

            if (this.constructing.contains(value)) {
                this.defaultSerializer.serialize(value, jg, sp);
                return;
            }

            MainCachedNode mainCache = value.getCachedNode();
            if (mainCache == null) {
                synchronized (value) {
                    int id = value.getId();
                    if (id == 0) {
                        jg.writeObject(value);
                        return;
                    }
                    InstancesTracker<Integer, MainCachedNode> cache = getNodeCache(value);
                    mainCache = cache.get(id);
                    if (mainCache != null) value.setCachedNode(mainCache);
                }
            }

            if (mainCache == null) {
                mainCache = getOrCreateNodeCache(value);
                value.setCachedNode(mainCache);
                if (this.generic) {
                    return;
                }
            }

            assert mapper == jg.getCodec();

            this.setConstructingNode(value);
            JsonNode updatedNode = mapper.valueToTree(value);
            this.doneConstructingNode(value);

            if (this.generic) {
                mainCache.setNode(updatedNode);

            } else synchronized (mainCache.alt) {
                CachedNode nodeToUpdate = (CachedNode)mainCache.alt.get(mapperType);
                if (nodeToUpdate == null) {
                    mainCache.alt.put(mapperType, new CachedNode(updatedNode));
                } else {
                    nodeToUpdate.setNode(updatedNode);
                }
            }

            jg.writeTree(updatedNode);
        }
    }

    public static class Resolvable<POJO extends NodeCacher<POJO>>
            extends IdCachingSerializer<POJO> implements ResolvableSerializer {

        private Resolvable(JsonSerializer<POJO> defaultSerializer,
                           ObjectMapper mapper) {
            super(defaultSerializer, mapper);
        }

        @Override
        public void resolve(SerializerProvider provider) throws JsonMappingException {
            ((ResolvableSerializer)this.defaultSerializer).resolve(provider);
        }
    }

    public static class ResolvableReset<POJO extends NodeCacher<POJO>>
            extends Reset<POJO> implements ResolvableSerializer {

        private ResolvableReset(JsonSerializer<POJO> defaultSerializer,
                                ObjectMapper mapper) {
            super(defaultSerializer, mapper);
        }

        @Override
        public void resolve(SerializerProvider provider) throws JsonMappingException {
            ((ResolvableSerializer)this.defaultSerializer).resolve(provider);
        }
    }



    private static ReadWriteLock<Map<Class<? extends NodeCacher>,
                                     InstancesTracker<Integer, MainCachedNode>>,
                                 InstancesTracker<Integer, MainCachedNode>>
            nodeCaches = ReadWriteLock.forMap(new LinkedHashMap<>());



    public static class CachedNode<NODE extends JsonNode> {
        private NODE node;
        private String string;

        private CachedNode(NODE node) {
            this.node = node;
            this.string = node.toString();
        }

        public NODE getNode() {
            return this.node.deepCopy();
        }

        public String getString() {
            return this.string;
        }

        protected void setNode(NODE node) {
            this.node = node;
            if (node != null) {
                this.string = node.toString();

            } else {
                this.string = null;
            }
        }
    }

    public static class MainCachedNode<POJO extends NodeCacher<POJO>> extends CachedNode<ObjectNode> {
        private final int id;
        private final Map<Class<? extends ObjectMapper>, CachedNode>
                alt = new LinkedHashMap<>();

        private final List<WeakReference<POJO>> pojos = new LinkedList<>();

        private MainCachedNode(ObjectNode node, int id) {
            super(node);
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public boolean represents(POJO pojo) {
            synchronized (this.pojos) {
                for (Iterator<WeakReference<POJO>> iterator = this.pojos.iterator();
                     iterator.hasNext(); ) {

                    WeakReference<POJO> ref = iterator.next();
                    POJO p = ref.get();
                    if (p == null) iterator.remove();
                    else if (p == pojo) return true;
                }
                return false;
            }
        }

        private void addToList(POJO pojo) {
            synchronized (this.pojos) {
                for (Iterator<WeakReference<POJO>> iterator = this.pojos.iterator();
                     iterator.hasNext(); ) {

                    WeakReference<POJO> ref = iterator.next();
                    POJO p = ref.get();
                    if (p == null) iterator.remove();
                    else if (p == pojo) return;
                }
                this.pojos.add(new WeakReference<>(pojo));
            }
        }

        private void removeFromList(POJO pojo) {
            synchronized (this.pojos) {
                for (Iterator<WeakReference<POJO>> iterator = this.pojos.iterator();
                     iterator.hasNext(); ) {

                    WeakReference<POJO> ref = iterator.next();
                    POJO p = ref.get();
                    if (p == null) iterator.remove();
                    else if (p == pojo) {
                        iterator.remove();
                        return;
                    }
                }
            }
        }

        public JsonNode getNodeForMapper(Class<? extends ObjectMapper> type) {
            if (Objects.equals(type, ObjectMapper.class)
                    || Objects.equals(type, CachingMapper.class)) {

                return this.getNode();
            }

            synchronized (this.alt) {
                CachedNode cachedNode = this.alt.get(type);
                if (cachedNode == null) return null;
                return cachedNode.getNode();
            }
        }

        public String getStringForMapperType(Class<? extends ObjectMapper> type) {
            synchronized (this.alt) {
                CachedNode cachedNode = this.alt.get(type);
                if (cachedNode == null) return null;
                return cachedNode.getString();
            }
        }
    }


    private static InstancesTracker<Integer, MainCachedNode>
            getNodeCache(NodeCacher pojo) {

        Class type = pojo.getClass();

        InstancesTracker<Integer, MainCachedNode> cache = nodeCaches.read(nc -> nc.get(type));
        if (cache != null) return cache;
        return nodeCaches.write(nc -> {
            InstancesTracker<Integer, MainCachedNode> cm = nc.get(type);
            if (cm == null) {
                cm = new InstancesTracker(new Type(Integer.class), new Type(MainCachedNode.class, type), null);
                nc.put(type, cm);
            }
            return cm;
        });
    }



    public static <POJO extends NodeCacher<POJO>> MainCachedNode<POJO>
            getOrCreateNodeCache(POJO pojo) {

        if (pojo == null) return null;

        int id = pojo.getId();
        if (id == 0) {
            throw new IllegalArgumentException("NodeCacher pojos must have a primary key id set before requesting a node cache!");
        }

        MainCachedNode<POJO> result = pojo.getCachedNode();
        if (result != null) {
            result.addToList(pojo);
            return result;
        }

        InstancesTracker<Integer, MainCachedNode> cache = getNodeCache(pojo);

        synchronized (pojo) {
            result = pojo.getCachedNode();
            // double-check, in case it was constructed while waiting for the lock
            if (result == null) {
                result = cache.get(id);
            }
            if (result != null) {
                result.addToList(pojo);
                return result;
            }
        }

        //construct OUTSIDE of the synchronized block.  Could cause deadlock
        ObjectNode node = new ObjectMapper().valueToTree(pojo);

        synchronized (pojo) {
            result = pojo.getCachedNode();
            if (result != null) return result;
            //triple check... in case of it was constructed while waiting for the lock, again...

            result = cache.getOrConstructWith(id, i -> new MainCachedNode(node, i));
        }
        result.addToList(pojo);
        return result;
    }

    public static <POJO extends NodeCacher<POJO>> MainCachedNode<POJO>
            updateNode(POJO pojo) {

        if (pojo == null) return null;
        MainCachedNode<POJO> node = pojo.getCachedNode();
        if (node == null) {
            InstancesTracker<Integer, MainCachedNode> cache = getNodeCache(pojo);
            node = cache.get(pojo.getId());
            if (node == null) {
                return getOrCreateNodeCache(pojo);
            }

        } else {
            assert node.id == pojo.getId();
        }

        node.setNode(new ObjectMapper().valueToTree(pojo));
        return node;
    }

    public static <POJO extends NodeCacher<POJO>> MainCachedNode<POJO>
            updateNode(POJO pojo, ObjectMapper mapper) {

        if (pojo == null || mapper == null) return null;
        Class<? extends ObjectMapper> mapperType = mapper.getClass();

        if (Objects.equals(mapperType, ObjectMapper.class)
            || Objects.equals(mapperType, CachingMapper.class)) {

            return updateNode(pojo);
        }

        MainCachedNode<POJO> cachedNode = pojo.getCachedNode();
        if (cachedNode == null) {
            InstancesTracker<Integer, MainCachedNode> cache = getNodeCache(pojo);
            cachedNode = cache.get(pojo.getId());
            if (cachedNode == null) {
                cachedNode = getOrCreateNodeCache(pojo);
            }

        } else {
            assert cachedNode.id == pojo.getId();
        }

        synchronized (cachedNode.alt) {
            JsonNode newNode = mapper.valueToTree(pojo);
            CachedNode alt = cachedNode.alt.get(mapperType);
            if (alt == null) {
                alt = new CachedNode(newNode);
                cachedNode.alt.put(mapperType, alt);

            } else {
                alt.setNode(newNode);
            }
        }
        return cachedNode;
    }
}

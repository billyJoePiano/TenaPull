package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.type.*;
import nessusTools.data.entity.template.*;
import nessusTools.util.Type;

import java.lang.reflect.*;
import java.util.*;

public class CachingMapper extends ObjectMapper {
    public static final String GET_CACHING_SERIALIZER = "getCachingSerializer";
    public static final String GET_CACHE_RESET_SERIALIZER = "getCacheResetSerializer";

    public static final CachingMapper mapper = new CachingMapper();
    public static final CachingMapper resetCaches = new CachingMapper(null);

    protected CachingMapper() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new CachedNodeModifier());
        this.registerModule(module);
        this.staticFetcher = GET_CACHING_SERIALIZER;
    }

    protected CachingMapper(Void resetCaches) {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new CachedNodeModifier());
        this.registerModule(module);
        this.staticFetcher = GET_CACHE_RESET_SERIALIZER;
    }

    private Map<Type, JsonSerializer> cachingSerializers = new LinkedHashMap<>();
    private final String staticFetcher;

    public class CachedNodeModifier extends BeanSerializerModifier {

        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                         BeanDescription desc,
                                                         List<BeanPropertyWriter> beanProperties) {

            for (ListIterator<BeanPropertyWriter> iterator = beanProperties.listIterator();
                    iterator.hasNext();) {

                BeanPropertyWriter writer = iterator.next();
                JavaType jt = writer.getType();
                if (jt == null) continue;
                Class type = jt.getRawClass();

                JsonSerializer defaultSerializer = writer.getSerializer();

                if (defaultSerializer == null) {
                    continue;
                }
                JsonSerializer newSerializer = checkForCachingSerializer(config, type, defaultSerializer);

                if (newSerializer != defaultSerializer) {
                    iterator.set(new CachingBeanPropertyWriter(writer, newSerializer));
                }

            }
            return beanProperties;
        }

        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            if (serializer instanceof IdCachingSerializer) return serializer;
            else return checkForCachingSerializer(config, beanDesc.getBeanClass(), serializer);
        }

        public JsonSerializer<?> checkForCachingSerializer(SerializationConfig config,
                                                           Class beanType,
                                                           JsonSerializer defaultSerializer) {

            if (!(DbPojo.class.isAssignableFrom(beanType))) return defaultSerializer;

            Type serializerType = new Type(defaultSerializer.getClass(), beanType);

            JsonSerializer<?> serializer = cachingSerializers.get(serializerType);
            if (serializer != null) return serializer;

            JsonSerializer<?> cachingSerializer;

            try {
                Method method = beanType.getMethod(CachingMapper.this.staticFetcher, JsonSerializer.class, ObjectMapper.class);
                cachingSerializer = (JsonSerializer)method.invoke(null, defaultSerializer, CachingMapper.this);

            } catch (Exception e) {
                return defaultSerializer;
            }

            cachingSerializers.put(serializerType, cachingSerializer);
            return cachingSerializer;
        }
    }

    public class CachingBeanPropertyWriter extends BeanPropertyWriter {
        protected CachingBeanPropertyWriter(BeanPropertyWriter overwriting, JsonSerializer cacher) {
            super(overwriting);
            this._serializer = cacher;
        }
    }
}

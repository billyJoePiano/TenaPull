package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.jetbrains.annotations.*;

import javax.json.*;
import javax.persistence.*;
import java.io.*;
import java.util.*;

// P parent , C child
@MappedSuperclass
public abstract class NestedJsonArray
                <P extends ExtensibleJsonPojo, C extends ObjectLookupPojo<C>>
        implements List<C> {

    public static final String EXTRA_JSON_KEY_IN_PARENT = "__nestedArrayContainer_extraJson__";

    public NestedJsonArray() { }

    public NestedJsonArray(List<C> list) {
        this.setList(list);
    }

    protected static <P extends ExtensibleJsonPojo,
                    W extends NestedJsonArray<P, C>,
                    C extends ObjectLookupPojo<C>   >
            W wrapIfNeeded(P parent, List<C> list, Class<W> wrapperType)
                throws IllegalStateException {

        if (wrapperType.isInstance(list)) {
            ((W)list).putExtraJsonIntoParent(parent);
            return (W)list;

        } else {
            W wrapper;
            try {
                wrapper = wrapperType.getDeclaredConstructor().newInstance();

            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            wrapper.setExtraJsonFromParent(parent);
            return wrapper;
        }
    }

    public abstract String getArrayKey();

    @JsonIgnore
    private List<C> list;

    @Transient
    @JsonIgnore
    private ObjectNode extraJson;

    @Transient
    @JsonAnyGetter
    public ObjectNode getExtraJson() {
        //TODO??? Does this need to be converted into a Map?
        return this.extraJson;
    }

    @Transient
    @JsonAnySetter
    public void putExtraJson(String key, Object value) {
        if (this.extraJson == null) {
            this.extraJson = new ObjectNode(JsonNodeFactory.instance);
        }
        JsonNode node;
        if (value instanceof JsonNode) {
            node = (JsonNode) value;
        } else {
            node = new ObjectMapper().convertValue(value, JsonNode.class);
        }
        this.extraJson.set(key, node);
    }

    @Transient
    @JsonIgnore
    public void setExtraJsonFromParent(P parent) throws ClassCastException {
        Map<String, JsonNode> map = parent.getExtraJsonMap();
        if (map == null) {
            this.extraJson = null;
            return;
        }

        JsonNode node = map.get(EXTRA_JSON_KEY_IN_PARENT + this.getClass().getSimpleName());

        if (node instanceof ObjectNode) {
            this.extraJson = (ObjectNode)node;

        } else {
            this.extraJson = null;
        }
    }

    @Transient
    @JsonIgnore
    public void putExtraJsonIntoParent(P parent) {
        if (this.extraJson == null) return;
        parent.putExtraJson(EXTRA_JSON_KEY_IN_PARENT + this.getClass().getSimpleName(),
                this.extraJson);
    }



    public static abstract class DeserializerTemplate
                    <W extends NestedJsonArray<?, C>, C extends ObjectLookupPojo<C>>
            extends JsonDeserializer<W> {

        public abstract JsonDeserializer<C> getChildDeserializer();

        public abstract W getNewWrapperInstance();

        @Override
        public W deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            W wrapper = getNewWrapperInstance();
            String ARRAY_KEY = wrapper.getArrayKey();

            JsonToken current;
            while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (current != JsonToken.FIELD_NAME) {
                    throw new JsonException(this.getClass().getCanonicalName() +
                            " expected FIELD_NAME token, not " + jp.currentToken());
                }

                String fieldName = jp.getCurrentName();
                jp.nextToken();

                if (Objects.equals(fieldName, ARRAY_KEY)) {
                    if (!jp.isExpectedStartArrayToken()) {
                        throw new JsonException(this.getClass().getCanonicalName() +
                                " expected ARRAY_START token, not" + jp.currentToken());
                    }

                    JsonDeserializer<C> deserializer = this.getChildDeserializer();

                    while (jp.nextToken() == JsonToken.START_OBJECT) {
                        wrapper.add(deserializer.deserialize(jp, ctxt));
                    }

                } else {
                    wrapper.putExtraJson(fieldName,
                                                jp.readValueAs(JsonNode.class));
                }
            }
            return wrapper;
        }
    }

    public static abstract class SerializerTemplate
                    <W extends NestedJsonArray<?, C>, C extends ObjectLookupPojo<C>>
            extends JsonSerializer<W> {

        @Override
        public void serialize(W wrapper,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {
            ObjectNode extraJson = wrapper.getExtraJson();
            if (extraJson != null) {
                jg.writeStartObject(extraJson);

            } else {
                jg.writeStartObject();
            }

            // can be overridden to add additional properties after the extraJson but before the array
            wrapper.writeBeforeArray(jg, sp);

            jg.writeFieldName(wrapper.getArrayKey());

            if (wrapper.getList() != null) {
                jg.writeObject(wrapper.getList());

            } else  {
                jg.writeStartArray();
                jg.writeEndArray();
            }

            jg.writeEndObject();
        }
    }

    // can be overridden to add additional properties after the extraJson but before the array
    protected void writeBeforeArray(JsonGenerator jg, SerializerProvider sp) throws IOException { }

    protected List<C> getList() {
        return this.list;
    }

    public void setList(List<C> list) {
        // short-circuit any recursive references
        if (list == this) return;
        if (list instanceof NestedJsonArray) {
            List<List<C>> found = new LinkedList<>();
            List<C> next = list;
            do {
                if (found.contains(next)) {
                    return;
                }
                found.add(next);
                next = ((NestedJsonArray<?, C>)list).list;
                if (next == this) return;

            } while (next instanceof NestedJsonArray);
        }
        this.list = list;
    }

    @Override
    @JsonIgnore
    public int size() {
        if (this.list == null) return 0;
        else return this.list.size();
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        if (this.list == null) return true;
        else return this.list.isEmpty();
    }

    @Override
    @JsonIgnore
    public boolean contains(Object o) {
        if (this.list == null) return false;
        else return this.list.contains(o);
    }

    @NotNull
    @Override
    @JsonIgnore
    public Iterator<C> iterator() {
        if (this.list == null) return Collections.emptyIterator();
        else return this.list.iterator();
    }

    @NotNull
    @Override
    @JsonIgnore
    public Object[] toArray() {
        if (this.list == null) return new Object[0];
        else return this.list.toArray();
    }

    @NotNull
    @Override
    @JsonIgnore
    public <T> T[] toArray(@NotNull T[] a) {
        if (this.list == null) return a;
        else return this.list.toArray(a);
    }

    @Override
    @JsonIgnore
    public boolean add(C child) {
        if (this.list == null) this.list = new LinkedList<>();
        return this.list.add(child);
    }

    @Override
    @JsonIgnore
    public boolean remove(Object o) {
        if (this.list == null) return false;
        else return this.list.remove(o);
    }

    @Override
    @JsonIgnore
    public boolean containsAll(@NotNull Collection<?> c) {
        if (this.list == null) return c.size() == 0;
        else return this.list.containsAll(c);
    }

    @Override
    @JsonIgnore
    public boolean addAll(@NotNull Collection<? extends C> c) {
        if (this.list == null) return (this.list = new LinkedList<>()).addAll(c);
        else return this.list.addAll(c);
    }

    @Override
    @JsonIgnore
    public boolean addAll(int index, @NotNull Collection<? extends C> c) {
        if (this.list == null) return (this.list = new LinkedList<>()).addAll(index, c);
        else return this.list.addAll(index, c);
    }

    @Override
    @JsonIgnore
    public boolean removeAll(@NotNull Collection<?> c) {
        if (this.list == null) return false;
        else return this.list.removeAll(c);
    }

    @Override
    @JsonIgnore
    public boolean retainAll(@NotNull Collection<?> c) {
        if (this.list == null) return false;
        else return this.list.retainAll(c);
    }

    @Override
    @JsonIgnore
    public void clear() {
        if (this.list != null) this.list.clear();
    }

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this.list == null) return o instanceof List && ((List)o).size() == 0;
        else return this.list.equals(o);
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        if (this.list == null) return 1;
        else return this.list.hashCode();
    }

    @Override
    @JsonIgnore
    public C get(int index) {
        if (this.list == null) throw new IndexOutOfBoundsException(index);
        else return this.list.get(index);
    }

    @Override
    @JsonIgnore
    public C set(int index, C element) {
        if (this.list == null) throw new IndexOutOfBoundsException(index);
        else return this.list.set(index, element);
    }

    @Override
    @JsonIgnore
    public void add(int index, C element) {
        if (this.list == null) {
            if (index != 0) throw new IndexOutOfBoundsException(index);
            (this.list = new LinkedList<>()).add(0, element);

        } else this.list.add(index, element);
    }

    @Override
    @JsonIgnore
    public C remove(int index) {
        if (this.list == null) throw new IndexOutOfBoundsException(index);
        else return this.list.remove(index);
    }

    @Override
    @JsonIgnore
    public int indexOf(Object o) {
        if (this.list == null) return -1;
        else return this.list.indexOf(o);
    }

    @Override
    @JsonIgnore
    public int lastIndexOf(Object o) {
        if (this.list == null) return -1;
        else return this.list.lastIndexOf(o);
    }

    @NotNull
    @Override
    @JsonIgnore
    public ListIterator<C> listIterator() {
        if (this.list == null) return Collections.emptyListIterator();
        else return this.list.listIterator();
    }

    @NotNull
    @Override
    @JsonIgnore
    public ListIterator<C> listIterator(int index) {
        if (this.list == null) {
            if (index != 0) throw new IndexOutOfBoundsException(index);
            return Collections.emptyListIterator();

        } else return this.list.listIterator(index);
    }

    @NotNull
    @Override
    @JsonIgnore
    public List<C> subList(int fromIndex, int toIndex) {
        if (this.list == null) {
            if (fromIndex != 0 || toIndex != 0) throw new IndexOutOfBoundsException();
            return Collections.emptyList();

        } else return this.list.subList(fromIndex, toIndex);
    }
}

package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import org.jetbrains.annotations.*;

import javax.json.*;
import javax.persistence.*;
import java.io.*;
import java.util.*;

public class SeverityCount implements List<SeverityLevelCount> {
    private static ObjectLookup.Deserializer<SeverityLevelCount>
            deserializer = new ObjectLookup.Deserializer<SeverityLevelCount>(SeverityLevelCount.class);

    public static final String EXTRA_JSON_KEY_IN_ScanHost = "severitycount";

    public static final String ARRAY_KEY_FOR_SeverityLevelCount = "item";

    public SeverityCount() { }

    public SeverityCount(List<SeverityLevelCount> item) {
        this.item = item;
    }

    @JsonProperty("item")
    private List<SeverityLevelCount> item;

    @Transient
    @JsonIgnore
    private ObjectNode extraJson;

    @Transient
    @JsonAnyGetter
    public ObjectNode getExtraJson() {
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
    public void setExtraJsonFromParent(ScanHost parent) throws ClassCastException {
        Map<String, JsonNode> map = parent.getExtraJsonMap();
        if (map == null) {
            this.extraJson = null;
            return;
        }

        JsonNode node = map.remove(EXTRA_JSON_KEY_IN_ScanHost);

        if (node == null) this.extraJson = null;
        else this.extraJson = (ObjectNode)node;
    }

    @Transient
    @JsonIgnore
    public void putExtraJsonIntoParent(ScanHost parent) {
        if (this.extraJson == null) return;
        parent.putExtraJson(EXTRA_JSON_KEY_IN_ScanHost, this.extraJson);
    }



    public static class Deserializer extends JsonDeserializer<List<SeverityLevelCount>> {
        @Override
        public List<SeverityLevelCount> deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            SeverityCount severityCount = new SeverityCount();

            JsonToken current;
            while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (current != JsonToken.FIELD_NAME) {
                    throw new JsonException(
                            "SeverityCount.Deserializer expected FIELD_NAME token, not "
                                    + jp.currentToken());
                }

                String fieldName = jp.getCurrentName();
                jp.nextToken();

                if (Objects.equals(fieldName, ARRAY_KEY_FOR_SeverityLevelCount)) {
                    if (!jp.isExpectedStartArrayToken()) {
                        throw new JsonException("SeverityCount.Deserializer expected ARRAY_START token, not"
                                                        + jp.currentToken());
                    }

                    while (jp.nextToken() == JsonToken.START_OBJECT) {
                        severityCount.add(deserializer.deserialize(jp, ctxt));
                    }

                } else {
                    severityCount.putExtraJson(fieldName,
                                                jp.readValueAs(JsonNode.class));
                }
            }
            return severityCount;
        }
    }

    public static class Serializer extends JsonSerializer<SeverityCount> {
        @Override
        public void serialize(SeverityCount severityCount,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {

            if (severityCount.extraJson != null) {
                jg.writeStartObject(severityCount.extraJson); // ??? TODO does this work ... write object but not close it?


                /*for (Iterator<Map.Entry<String,JsonNode>>
                            iterator = severityCount.extraJson.fields();
                        iterator.hasNext();) {

                    Map.Entry<String, JsonNode> entry = iterator.next();
                    jg.writeF

                }*/
            } else {
                jg.writeStartObject();
            }

            jg.writeFieldName(ARRAY_KEY_FOR_SeverityLevelCount);

            if (severityCount.item != null) {
                jg.writeObject(severityCount.item);

            } else  {
                jg.writeStartArray();
                jg.writeEndArray();
            }
        }
    }


    public List<SeverityLevelCount> getItem() {
        return this.item;
    }

    public void setItem(List<SeverityLevelCount> item) {
        this.item = item;
    }

    @Override
    @JsonIgnore
    public int size() {
        if (this.item == null) return 0;
        else return this.item.size();
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        if (this.item == null) return true;
        else return this.item.isEmpty();
    }

    @Override
    @JsonIgnore
    public boolean contains(Object o) {
        if (this.item == null) return false;
        else return this.item.contains(o);
    }

    @NotNull
    @Override
    @JsonIgnore
    public Iterator<SeverityLevelCount> iterator() {
        if (this.item == null) return Collections.emptyIterator();
        else return this.item.iterator();
    }

    @NotNull
    @Override
    @JsonIgnore
    public Object[] toArray() {
        if (this.item == null) return new Object[0];
        else return this.item.toArray();
    }

    @NotNull
    @Override
    @JsonIgnore
    public <T> T[] toArray(@NotNull T[] a) {
        if (this.item == null) return a;
        else return this.item.toArray(a);
    }

    @Override
    @JsonIgnore
    public boolean add(SeverityLevelCount severityLevelCount) {
        if (this.item == null) this.item = new LinkedList<>();
        return this.item.add(severityLevelCount);
    }

    @Override
    @JsonIgnore
    public boolean remove(Object o) {
        if (this.item == null) return false;
        else return this.item.remove(o);
    }

    @Override
    @JsonIgnore
    public boolean containsAll(@NotNull Collection<?> c) {
        if (this.item == null) return c.size() == 0;
        else return this.item.containsAll(c);
    }

    @Override
    @JsonIgnore
    public boolean addAll(@NotNull Collection<? extends SeverityLevelCount> c) {
        if (this.item == null) return (this.item = new LinkedList<>()).addAll(c);
        else return this.item.addAll(c);
    }

    @Override
    @JsonIgnore
    public boolean addAll(int index, @NotNull Collection<? extends SeverityLevelCount> c) {
        if (this.item == null) return (this.item = new LinkedList<>()).addAll(index, c);
        else return this.item.addAll(index, c);
    }

    @Override
    @JsonIgnore
    public boolean removeAll(@NotNull Collection<?> c) {
        if (this.item == null) return false;
        else return this.item.removeAll(c);
    }

    @Override
    @JsonIgnore
    public boolean retainAll(@NotNull Collection<?> c) {
        if (this.item == null) return false;
        else return this.item.retainAll(c);
    }

    @Override
    @JsonIgnore
    public void clear() {
        if (this.item != null) this.item.clear();
    }

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this.item == null) return o instanceof List && ((List)o).size() == 0;
        else return this.item.equals(o);
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        if (this.item == null) return 1;
        else return this.item.hashCode();
    }

    @Override
    @JsonIgnore
    public SeverityLevelCount get(int index) {
        if (this.item == null) throw new IndexOutOfBoundsException(index);
        else return this.item.get(index);
    }

    @Override
    @JsonIgnore
    public SeverityLevelCount set(int index, SeverityLevelCount element) {
        if (this.item == null) throw new IndexOutOfBoundsException(index);
        else return this.item.set(index, element);
    }

    @Override
    @JsonIgnore
    public void add(int index, SeverityLevelCount element) {
        if (this.item == null) {
            if (index != 0) throw new IndexOutOfBoundsException(index);
            (this.item = new LinkedList<>()).add(0, element);

        } else this.item.add(index, element);
    }

    @Override
    @JsonIgnore
    public SeverityLevelCount remove(int index) {
        if (this.item == null) throw new IndexOutOfBoundsException(index);
        else return this.item.remove(index);
    }

    @Override
    @JsonIgnore
    public int indexOf(Object o) {
        if (this.item == null) return -1;
        else return this.item.indexOf(o);
    }

    @Override
    @JsonIgnore
    public int lastIndexOf(Object o) {
        if (this.item == null) return -1;
        else return this.item.lastIndexOf(o);
    }

    @NotNull
    @Override
    @JsonIgnore
    public ListIterator<SeverityLevelCount> listIterator() {
        if (this.item == null) return Collections.emptyListIterator();
        else return this.item.listIterator();
    }

    @NotNull
    @Override
    @JsonIgnore
    public ListIterator<SeverityLevelCount> listIterator(int index) {
        if (this.item == null) {
            if (index != 0) throw new IndexOutOfBoundsException(index);
            return Collections.emptyListIterator();

        } else return this.item.listIterator(index);
    }

    @NotNull
    @Override
    @JsonIgnore
    public List<SeverityLevelCount> subList(int fromIndex, int toIndex) {
        if (this.item == null) {
            if (fromIndex != 0 || toIndex != 0) throw new IndexOutOfBoundsException();
            return Collections.emptyList();

        } else return this.item.subList(fromIndex, toIndex);
    }
}

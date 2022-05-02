package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.template.*;

import java.io.*;
import java.util.*;

public class SummarySerializer<S extends DbPojo, D extends DbPojo>
        extends JsonSerializer<SummarySerializer.Summary<S, D>> {

    public interface Summary<S extends DbPojo, D extends DbPojo> {
        S getSummary();
        D getDetails();
        String getName();
        default String getDetailsKey() {
            return "info";
        }
        default Integer getId() {
            return null;
        }
    }

    @Override
    public void serialize(Summary<S, D> container,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        S summary = container.getSummary();
        D deets = container.getDetails();

        if (summary == null) {
            String name = container.getName();
            if (deets == null) {
                jg.writeStartObject();
                jg.writeStringField("NessusTools error",
                        "Cached data from both the " + name + " summary and "
                                + name + " details couldn't be found");

                return;
            }

            jg.writeStartObject();
            Integer id = container.getId();
            if (id != null) {
                jg.writeNumberField("id", id);
            }
            jg.writeStringField("NessusTools error",
                    "Cached data from the " + name + " summary couldn't be found");

            jg.writeObjectField(container.getDetailsKey(), deets);
            return;

        }



        ObjectMapper mapper = new SplunkOutputSerializer();

        ObjectNode sum = mapper.valueToTree(summary);
        ObjectNode info;

        if (deets == null) {
            info = mapper.createObjectNode();
            info.put("NessusTools error",
                    "Cached data from " + container.getName() + " details couldn't be found.");
        } else {
            info = mapper.valueToTree(deets);
            for (Iterator<Map.Entry<String, JsonNode>> iterator = info.fields();
                 iterator.hasNext();) {

                Map.Entry<String, JsonNode> entry = iterator.next();
                String key = entry.getKey();
                Object value = entry.getValue();
                if (sum.has(key) && Objects.equals(sum.get(key), value)) {
                    iterator.remove();
                }
            }
        }

        sum.set(container.getDetailsKey(), info);

        jg.writeObject(sum);
    }
}
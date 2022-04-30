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
    public void serialize(Summary<S, D> summary,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        S s = summary.getSummary();
        D d = summary.getDetails();

        if (s == null) {
            String name = summary.getName();
            if (d == null) {
                jg.writeStartObject();
                jg.writeStringField("NessusTools error",
                        "Cached data from both the " + name + " summary and "
                                + name + " details couldn't be found");

                return;
            }

            jg.writeStartObject();
            Integer id = summary.getId();
            if (id != null) {
                jg.writeNumberField("id", id);
            }
            jg.writeStringField("NessusTools error",
                    "Cached data from the " + name + " summary couldn't be found");

            jg.writeObjectField(summary.getDetailsKey(), d);
            return;

        }



        ObjectMapper mapper = new SplunkOutputSerializer();

        ObjectNode sum = mapper.valueToTree(s);
        ObjectNode info;

        if (d == null) {
            info = mapper.createObjectNode();
            info.put("NessusTools error",
                    "Cached data from " + summary.getName() + " details couldn't be found.");
        } else {
            info = mapper.valueToTree(d);
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

        sum.set(summary.getDetailsKey(), info);

        jg.writeObject(sum);
    }
}
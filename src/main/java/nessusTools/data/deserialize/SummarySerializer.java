package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.*;

import java.io.*;
import java.util.*;

public class SummarySerializer<S extends DbPojo, D extends DbPojo>
        extends JsonSerializer<SummarySerializer.Summary<S, D>> {

    public interface Summary<S extends DbPojo, D extends DbPojo> {
        S getSummary();
        D getDetails();
        ExtraJson getOther();
        String getName();
        default String getDetailsKey() {
            return "info";
        }
        default Integer getOptionalId() {
            return null;
        }
    }

    @Override
    public void serialize(Summary<S, D> container,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        S summary = container.getSummary();
        D deets = container.getDetails();
        Integer id = container.getOptionalId();
        ExtraJson xtra = container.getOther();
        String name = container.getName();

        if (summary == null) {
            if (deets == null) {
                jg.writeStartObject();
                jg.writeStringField("NessusTools error",
                        "Cached data from both the " + name + " summary and "
                                + name + " details couldn't be found");

                return;
            }

            jg.writeStartObject();
            if (id != null) {
                jg.writeNumberField("id", id);
            }
            jg.writeStringField("NessusTools error",
                    "Cached data from the " + name + " summary couldn't be found");

            jg.writeObjectField(container.getDetailsKey(), deets);

            if (xtra != null) {
                jg.writeObjectField(name + "_other_info", xtra.getValue().getView());
            }

            return;

        }


        ObjectMapper mapper = (ObjectMapper)jg.getCodec();
        ObjectNode sum;
        if (id != null) {
            sum = mapper.createObjectNode();
            sum.put("id", id);
            sum.setAll((ObjectNode)mapper.valueToTree(summary));

        } else {
            sum = mapper.valueToTree(summary);
        }


        ObjectNode info;
        if (deets == null) {
            info = mapper.createObjectNode();
            info.put("NessusTools error",
                    "Cached data from " + name + " details couldn't be found.");
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
        if (xtra != null) {
            sum.set(name + "_other", mapper.valueToTree(xtra.getValue().getView()));
        }

        jg.writeObject(sum);
    }
}
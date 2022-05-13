package tenapull.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.template.*;

import java.io.*;
import java.util.*;

/**
 * Used when outputting records, for serializing the "summary" wrapper objects inside the
 * HostVulnerabilityOutput entity.  These summary wrapper objects must implement the Summary
 * interface provided as an inner interface in this serializer.
 *
 * @param <S> The general-info ("summary") entity from the DB
 * @param <D> The more detailed-info ("details") entity from the DB
 */
public class SummarySerializer<S extends DbPojo, D extends DbPojo>
        extends JsonSerializer<SummarySerializer.Summary<S, D>> {

    /**
     * The interface that must be implemented by the summary wrappers being serialized
     *
     * @param <S> The general-info ("summary") entity from the DB
     * @param <D> The more detailed-info ("details") entity from the DB
     */
    public interface Summary<S extends DbPojo, D extends DbPojo> {
        /**
         * Gets the "summary" entity from the DB
         *
         * @return the summary entity
         */
        S getSummary();

        /**
         * Gets the "details" or "info" entity from the DB
         *
         * @return the details entity
         */
        D getDetails();

        /**
         * Get any extra json to be included, or null if there is none
         *
         * @return any ExtraJson to be included
         */
        ExtraJson getOther();

        /**
         * Get the name of the entity being summarized
         * @return the name of the entity being summarized
         */
        String getName();

        /**
         * Get the key to be used for the "details" entity.  By default it is "info"
         *
         * @return the key to be used for the details entity
         */
        default String getDetailsKey() {
            return "info";
        }

        /**
         * Optionally returns an Integer id to be serialized as the first entry in the
         * summary wrapper.  If null is returned, the id field is omitted
         *
         * @return an id to be serialized, or null if omitting the id
         */
        default Integer getOptionalId() {
            return null;
        }
    }

    /**
     * Obtains the summary and detail entities from a summary wrapper,
     * and converts them into JsonObjects using the same mapper being invoked.
     * Then takes the two JsonObjects and compares keys, eliminating any redundant
     * keys in the detail entity (as long as the values are identical with the summary
     * entity's).  Then nests the details entity in the summary entity using the
     * key provided by getDetailsKey(), and writes the resulting JsonObject to the
     * current serialization JsonGenerator
     *
     * @param container the wrapper entity implementing the SummarySerializer.Summary interface
     * @param jg
     * @param sp
     * @throws IOException
     */
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
                jg.writeStringField("TenaPull error",
                        "Cached data from both the " + name + " summary and "
                                + name + " details couldn't be found");

                return;
            }

            jg.writeStartObject();
            if (id != null) {
                jg.writeNumberField("id", id);
            }
            jg.writeStringField("TenaPull error",
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
            info.put("TenaPull error",
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
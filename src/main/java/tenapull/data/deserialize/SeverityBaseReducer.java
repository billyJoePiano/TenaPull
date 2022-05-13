package tenapull.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.objectLookup.*;

import java.io.*;

/**
 * Serializer for simplifying a SeverityBase bean into a single text value, used for
 * serializing the output.
 */
public class SeverityBaseReducer extends JsonSerializer<SeverityBase> {


    /**
     * Simplifies a SeverityBase bean into a single text value, representing the display
     * value of the SeverityBase if it is not null, or the value (abbreviated) if that is not null,
     * or other the ExtraJson if that is not null.  Writes null if none can be found
     *
     * @param sc the SeverityBase bean to be serialized
     * @param jg
     * @param sp
     * @throws IOException
     */
    @Override
    public void serialize(SeverityBase sc,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        if (sc == null) {
            jg.writeNull();
            return;
        }

        String display = sc.getDisplay();
        if (display == null) {
            display = sc.getValue();
            if (display == null) {
                ExtraJson ej = sc.getExtraJson();
                if (ej != null && ej.size() > 0) {
                    jg.writeObject(ej.getValue().getView());

                } else {
                    jg.writeNull();
                }
                return;
            }
        }
        jg.writeString(display);
    }
}
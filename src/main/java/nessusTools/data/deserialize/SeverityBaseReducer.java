package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;

import java.io.*;
import java.util.*;

public class SeverityBaseReducer extends JsonSerializer<SeverityBase> {


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
            display = sc.getDisplay();
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
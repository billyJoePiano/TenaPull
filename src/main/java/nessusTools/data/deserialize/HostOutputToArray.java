package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.splunk.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.sql.*;
import java.util.*;

public class HostOutputToArray extends JsonSerializer<HostOutput> {
    Logger logger = LogManager.getLogger(HostOutputToArray.class);

    @Override
    public void serialize(HostOutput output, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (output != null) {
            List<HostVulnerabilityOutput> list = output.getVulnerabilities();
            if (list != null) {
                jg.writeObject(list);
                return;
            }
        }
        jg.writeStartArray();
        jg.writeEndArray();
    }

}

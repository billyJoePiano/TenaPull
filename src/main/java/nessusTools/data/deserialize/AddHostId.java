package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import org.apache.logging.log4j.*;

import javax.json.*;
import java.io.*;
import java.util.*;

public class AddHostId extends JsonSerializer<List<Vulnerability>> {

    public AddHostId() { }

    @Override
    public void serialize(List<Vulnerability> vulns,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        if (vulns == null) {
            jg.writeStartArray();
            jg.writeEndArray();
            return;
        }

        Object parent = jg.getCurrentValue();
        if (!(parent instanceof ScanHostResponse)) {
            throw new JsonException("AddHostId serializer should only be used with Vulnerability list of ScanHostResponse");
        }

        ScanHostResponse response = (ScanHostResponse) parent;

        ScanHost host = response.getHost();
        if (host == null) {
            host = ScanHost.dao.getById(response.getId());
            if (host == null) {
                throw new JsonException("Could not find ScanHost with id " + response.getId());
            }
            response.setHost(host);
        }

        Integer hostId = host.getHostId();
        if (hostId == null) {
            throw new JsonException("ScanHost did not have a host id set\n" + host);
        }

        int id = hostId;

        jg.writeStartArray();

        for (Vulnerability vuln : vulns) {
            if (vulns == null) {
                jg.writeNull();
                continue;
            }

            ObjectNode node = vuln.toJsonNode();
            node.put("host_id", id);
            jg.writeObject(node);
        }

        jg.writeEndArray();

    }
}
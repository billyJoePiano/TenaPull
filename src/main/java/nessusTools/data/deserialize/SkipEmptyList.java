package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.util.*;

public class SkipEmptyList {
    public static class Serializer<T> extends JsonSerializer<List<T>> {
        @Override
        public void serialize(List<T> list,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {
            
            if (list == null || list.size() <= 0) return;
            jg.writeStartArray(list);
            jg.writeEndArray();
        }
    }
}

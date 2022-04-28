package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.util.*;

public class Lists {
    public static class EmptyToNullSerializer<T> extends JsonSerializer<List<T>> {
        @Override
        public void serialize(List<T> list,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {
            
            if (list == null || list.size() <= 0) {
                jg.writeNull();

            } else {
                jg.writeObject(list);
            }

        }
    }

    
    public static class SortSerializer<T extends Comparable> extends JsonSerializer<List<T>> {
        @Override
        public void serialize(List<T> list,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {

            if (list == null) {
                jg.writeNull();

            } else {
                Collections.sort(list);
                jg.writeObject(list);
            }
        }
    }
}

package tenapull.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.util.*;

/**
 * Utility serializers for serializing empty lists as null, and for sorting lists prior to
 * serializing
 */
public class Lists {
    /**
     * Serializer which converts an empty list to a null JSON output
     * @param <T> the List type parameter
     */
    public static class EmptyToNullSerializer<T> extends JsonSerializer<List<T>> {
        /**
         * Checks if a list is empty, and serializes it as a null JSON output if it is.
         * Otherwise, serializes it normally
         * @param list
         * @param jg
         * @param sp
         * @throws IOException
         */
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

    /**
     * Serializer which sorts a list of Comparables before serializing it
     * @param <T>
     */
    public static class SortSerializer<T extends Comparable> extends JsonSerializer<List<T>> {
        /**
         * Takes a list of objects implementing the Comparable interface, sorts them using
         * Collections.sort(list), and then serializes them normally
         *
         * @param list
         * @param jg
         * @param sp
         * @throws IOException
         */
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

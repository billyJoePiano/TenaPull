package nessusTools.run;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Quick and dirty utility for filtering all of the JSON outputs in demo-output, to
 * include only the ones with plugin data.  Outputs the results to filteredForPlugin.json
 */
public class FilterForPlugin {
    /**
     * Quick and dirty utility for filtering all the JSON outputs in demo-output directory, to
     * include only the ones with plugin data.  Outputs the results to filteredForPlugin.json
     *
     * @param args ignored
     * @throws IOException if there was an IO exception while reading/writing files
     */
    public static void main(String args[]) throws IOException {
        List<JsonNode> nodes = new LinkedList();
        ObjectMapper mapper = new ObjectMapper();
        Var.Int count = new Var.Int();

        Files.list(new File("demo-output").toPath()).forEach(path -> {
            File file = path.toFile();
            String p = file.getPath();
            if (p.length() >= 22
                    && Objects.equals(p.substring(p.length() - 22), "filteredForPlugin.json")) {
                return;
            }

            JsonNode array;
            try {
                array = mapper.readTree(file);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (JsonNode node : array) {
                count.value++;
                if (node.has("plugin")) {
                    nodes.add(node);
                }
            }
        });

        mapper.writeValue(new FileOutputStream("demo-output/filteredForPlugin.json"), nodes);
    }

}

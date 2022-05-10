package nessusTools.run;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FilterForPlugin {
    public static void main(String args[]) throws IOException {
        List<JsonNode> nodes = new LinkedList();
        ObjectMapper mapper = new ObjectMapper();
        Var.Int count = new Var.Int();

        Files.list(new File("demo-output").toPath()).forEach(path -> {
            JsonNode array;
            try {
                array = mapper.readTree(path.toFile());

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

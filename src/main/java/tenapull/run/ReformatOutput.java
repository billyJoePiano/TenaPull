package tenapull.run;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.client.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.splunk.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class ReformatOutput extends Job {
    private static final Logger logger = LogManager.getLogger(ReformatOutput.class);
    public static final String OUTPUT_DIR = Main.getConfig("output.dir");
    private static final String[] FIELD_ORDER = HostVulnerabilityOutput.class.getAnnotation(JsonPropertyOrder.class).value();
    private static final TextNode SCANNER = TextNode.valueOf(Main.getConfig("output.scanner"));
    public static final Integer TRUNCATE = Main.parseTruncate();
    public static final boolean SEPARATE_OUTPUTS = Main.hasConfig("output.separate");

    private File file;

    public ReformatOutput(File file) throws NullPointerException {
        if (file == null) throw new NullPointerException();
        this.file = file;
    }

    @Override
    protected boolean isReady() {
        if (this.file.exists()) return true;

        logger.error("Could not find file " + file.getAbsolutePath());
        this.failed();
        return false;
    }

    @Override
    protected void fetch(NessusClient client) {

    }

    private List<ObjectNode> output;
    private ObjectMapper mapper;
    private boolean inputWasArray = false;

    @Override
    protected void process() throws IOException {
        mapper = new ObjectMapper();
        JsonNode input = mapper.readTree(file);

        switch (input.getNodeType()) {
            case ARRAY:
                inputWasArray = true;
                processArray((ArrayNode) input);
                return;

            case OBJECT:
                output = new ArrayList(1);
                processSeparate((ObjectNode)input);
                return;

            default:
                logger.error("INVALID JSON INPUT, MUST BE AN OBJECT OR ARRAY:n" + input);
                this.failed();
        }
    }

    private void processArray(ArrayNode input) {
        output = new ArrayList(input.size());

        for (JsonNode o : input) {
            processSeparate((ObjectNode) o);
        }
    }

    private void processSeparate(ObjectNode input) {
        input.remove("plugin_error");
        input.remove("scanner_url");
        input.remove("scanner");

        if (SplunkOutputMapper.TRUNCATE != null) {
            input = truncateObj(input);
        }

        ObjectNode newObj = mapper.createObjectNode();
        newObj.set("scanner", SCANNER);

        for (String field : FIELD_ORDER) {
            if (!input.has(field)) continue;
            newObj.set(field, input.remove(field));
        }
        for (Iterator<String> iterator = input.fieldNames();
             iterator.hasNext();) {

            String field = iterator.next();
            newObj.set(field, input.get(field));
        }

        output.add(newObj);
    }

    @Override
    protected void output() throws IOException {
        if (SEPARATE_OUTPUTS) {
            this.separateOutputs();

        } else {
            try (OutputStreamWriter writer
                    = new OutputStreamWriter(new FileOutputStream(OUTPUT_DIR + this.file.getName()))) {

                if (this.inputWasArray && this.output.size() > 0) {
                    mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
                    writer.write("[\n");

                    boolean firstIteration = true;

                    for (JsonNode node : this.output) {
                        if (firstIteration) {
                            firstIteration = false;

                        } else {
                            writer.write(",\n");
                        }

                        mapper.writeValue(writer, node);
                    }

                    writer.write("\n]");

                } else {
                    mapper.writeValue(writer, output);
                }
            }
        }
        logger.info(this.file.getName());
    }

    private void separateOutputs() throws IOException {
        String prefix = this.file.getName();
        boolean jsonExt = prefix.length() >= 5
                && Objects.equals(prefix.substring(prefix.length() - 5), ".json");

        if (jsonExt) {
            prefix = prefix.substring(0, prefix.length() - 5);
        }

        if (this.inputWasArray) {
            int i = 0;

            for (ObjectNode node : this.output) {
                String filename = prefix + "_" + (i++);
                if (jsonExt) {
                    filename += ".json";
                }

                File out = new File(OUTPUT_DIR + filename);
                mapper.writeValue(out, node);
            }

        } else if (output.size() == 1) {
            String filename = prefix + "_0";
            if (jsonExt) {
                filename += ".json";
            }

            File out = new File(OUTPUT_DIR + filename);
            mapper.writeValue(out, output.get(0));

        } else if (output.size() != 0) {
            this.failed();
            logger.error("UNEXPECTED OUTPUT SIZE:\n" + output);
        }
    }


    private ObjectNode truncateObj(ObjectNode obj) {
        ObjectNode newObj = mapper.createObjectNode();
        for (Iterator<String> iterator = obj.fieldNames();
             iterator.hasNext();) {

            String field = iterator.next();
            JsonNode node = obj.get(field);

            newObj.set(field, truncate(node));
        }
        return newObj;
    }

    private JsonNode truncate(JsonNode node) {
        if (node == null) return null;
        switch (node.getNodeType()) {
            case OBJECT:
                return truncateObj((ObjectNode) node);

            case ARRAY:
                return truncateArr((ArrayNode) node);

            case STRING:
                TextNode text = ((TextNode) node);
                String str = text.textValue();
                if (str.length() > TRUNCATE) {
                    return TextNode.valueOf(str.substring(0, TRUNCATE) + Truncater.TRUNCATE_NOTICE);
                }

            default:
                return node;
        }
    }

    private ArrayNode truncateArr(ArrayNode arr) {
        ArrayNode newArr = mapper.createArrayNode();
        for (JsonNode node : arr) {
            newArr.add(truncate(node));
        }
        return newArr;
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        logger.error("Error processing input from file " + file.getAbsolutePath(), e);
        return false;
    }
}

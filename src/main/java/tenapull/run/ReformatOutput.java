package tenapull.run;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.client.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.splunk.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

public class ReformatOutput extends Job {
    private static final Logger logger = LogManager.getLogger(ReformatOutput.class);
    public static final String OUTPUT_DIR = Main.getConfig("output.dir");
    private static final String[] FIELD_ORDER = HostVulnerabilityOutput.class.getAnnotation(JsonPropertyOrder.class).value();
    private static final TextNode SCANNER = TextNode.valueOf(Main.getConfig("output.scanner"));
    public static final Integer TRUNCATE = Main.getTruncate();

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

    @Override
    protected void process() throws Exception {
        mapper = new ObjectMapper();
        JsonNode input = mapper.readTree(file);
        output = new ArrayList(input.size());

        for (JsonNode o : input) {
            ObjectNode obj = (ObjectNode) o;
            obj.remove("plugin_error");
            obj.remove("scanner_url");
            obj.remove("scanner");

            if (SplunkOutputMapper.TRUNCATE != null) {
                obj = truncateObj(obj);
            }

            ObjectNode newObj = mapper.createObjectNode();
            newObj.set("scanner", SCANNER);

            for (String field : FIELD_ORDER) {
                if (!obj.has(field)) continue;
                newObj.set(field, obj.remove(field));
            }
            for (Iterator<String> iterator = obj.fieldNames();
                    iterator.hasNext();) {

                String field = iterator.next();
                newObj.set(field, obj.get(field));
            }

            output.add(newObj);
        }
    }

    @Override
    protected void output() throws Exception {
        File out = new File(OUTPUT_DIR + this.file.getName());
        mapper.writeValue(out, output);
        logger.info(this.file.getName());
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

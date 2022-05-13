package tenapull.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.template.*;

import java.io.*;

public class Truncater extends JsonSerializer<StringLookupPojo> {
    public static final String TRUNCATE_NOTICE = "...<TRUNCATED>";

    @Override
    public void serialize(StringLookupPojo value, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (value == null) {
            jg.writeNull();
            return;
        }

        String str = value.getValue();
        if (str.length() > SplunkOutputMapper.TRUNCATE) {
            str = str.substring(0, SplunkOutputMapper.TRUNCATE) + TRUNCATE_NOTICE;
        }

        jg.writeString(str);
    }
}

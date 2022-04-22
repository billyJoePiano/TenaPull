package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;

import nessusTools.data.entity.template.*;

public class ResponseReader<R extends NessusResponse> extends ObjectMapper {
    private final Class<R> type;
    private final R response;

    public ResponseReader(Class<R> type, R response) {
        this.type = type;
        this.response = response;
    }
}
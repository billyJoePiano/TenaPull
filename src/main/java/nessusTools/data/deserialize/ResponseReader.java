package nessusTools.data.deserialize;

import com.fasterxml.jackson.databind.*;
import nessusTools.client.response.*;

public class ResponseReader<R extends NessusResponse> extends ObjectMapper {
    private final Class<R> type;
    private final R response;

    public ResponseReader(Class<R> type, R response) {
        this.type = type;
        this.response = response;
    }
}

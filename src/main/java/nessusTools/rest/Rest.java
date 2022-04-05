package nessusTools.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.*;

@ApplicationPath("/service")
public class Rest extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                ScanRest.class
            );
    }

}

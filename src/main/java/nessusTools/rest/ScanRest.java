package nessusTools.rest;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import nessusTools.data.entity.*;
import nessusTools.data.entity.response.*;
import org.apache.logging.log4j.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/scans")
public class ScanRest {
    private static Logger logger = LogManager.getLogger(ScanRest.class);

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getScan(@PathParam("id") String idStr) {
        int id;
        try {
            id = Integer.parseInt(idStr);

        } catch (NumberFormatException e) {
            try {
                return Response.status(400).entity(
                        "{ \"error\":\"Not a valid id number\",\"id\":"
                                + (new ObjectMapper().writeValueAsString(idStr))
                                + "}").build();

            } catch (JsonProcessingException je) {
                logger.error(je);
                return Response.status(400).entity(
                        "{ \"error\":\"Not a valid id number\" }").build();
            }
        }

        ScanInfo info = ScanInfo.dao.getById(id);

        if (info == null) {
            return Response.status(404).entity(
                    "{ \"error\":\"Could not find scan id " +  id + "\" }").build();
        }

        try {
            return Response.status(200).entity(
                    new ObjectMapper().writeValueAsString(info)).build();

        } catch (JsonProcessingException e) {
            logger.error(e);
            return Response.status(500).entity(
                    "{ \"error\":\"Internal server error processing request\" }").build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getScans() {
        IndexResponse all = new IndexResponse();
        all.setScans(Scan.dao.getAll());
        all.setFolders(Folder.dao.getAll());

        try {
            return Response.status(200).entity(
                    new ObjectMapper().writeValueAsString(all)).build();

        } catch (JsonProcessingException e) {
            logger.error(e);
            return Response.status(500).entity(
                    "{ \"error\":\"Internal server error processing request\" }").build();
        }

    }
}

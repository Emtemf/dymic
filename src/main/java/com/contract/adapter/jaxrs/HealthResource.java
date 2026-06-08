package com.contract.adapter.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

/**
 * Health check endpoint exposed via CXF JAX-RS.
 *
 * <p>Full URL: {@code http://localhost:8888/api/v2/health}</p>
 */
@Component
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        return Response.ok("{\"status\":\"UP\",\"framework\":\"cxf-jaxrs\"}").build();
    }
}

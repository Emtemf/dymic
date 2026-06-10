package com.contract.adapter.jaxrs;

import com.contract.application.template.DataSourceQueryFacadeService;
import com.contract.application.template.dto.DataSourceQueryDTO;
import com.contract.application.template.dto.OptionDataDTO;
import com.contract.common.result.Result;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/ui/data-sources")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class DataSourceQueryResource {

    private final DataSourceQueryFacadeService service;

    @GET
    @Path("/query")
    public Response queryAll(@QueryParam("type") String type) {
        List<DataSourceQueryDTO> data = (type == null || type.isBlank())
            ? service.queryAll()
            : service.queryByType(type);
        return Response.ok(Result.ok(data)).build();
    }

    @GET
    @Path("/{providerId}/execute")
    public Response execute(@PathParam("providerId") Long providerId) {
        List<OptionDataDTO> data = service.executeQuery(providerId);
        return Response.ok(Result.ok(data)).build();
    }
}

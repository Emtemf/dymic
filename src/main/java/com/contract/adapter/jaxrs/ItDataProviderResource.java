package com.contract.adapter.jaxrs;

import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.common.result.Result;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/it/data-providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ItDataProviderResource {

    private final DataProviderService service;

    @POST
    public Response create(DataProviderCreateDTO dto) {
        DataProviderDTO result = service.create(dto);
        return Response.ok(Result.ok(result)).build();
    }

    @GET
    public Response list() {
        List<DataProviderDTO> result = service.listITConfigs();
        return Response.ok(Result.ok(result)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        DataProviderDTO result = service.getById(id);
        return Response.ok(Result.ok(result)).build();
    }
}

package com.contract.adapter.jaxrs;

import com.contract.application.template.DataSourceConfigService;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.common.result.Result;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/config/business-data-sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class BusinessDataSourceResource {

    private final DataSourceConfigService service;

    @POST
    public Response create(BusinessDataSourceRequest request) {
        DataProviderDTO result = service.create(request);
        return Response.ok(Result.ok(result)).build();
    }

    @GET
    public Response list() {
        List<DataProviderDTO> result = service.listBusinessConfigs();
        return Response.ok(Result.ok(result)).build();
    }
}

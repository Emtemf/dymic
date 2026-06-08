package com.contract.adapter.jaxrs;

import com.contract.application.template.SchemaService;
import com.contract.application.template.dto.SchemaDTO;
import com.contract.application.template.dto.SchemaSaveDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Path("/templates/{templateId}/versions/{versionId}/schema")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class SchemaResource {

    private final SchemaService schemaService;

    @GET
    public Response getSchema(
        @PathParam("templateId") Long templateId,
        @PathParam("versionId") Long versionId
    ) {
        SchemaDTO schema = schemaService.getSchema(templateId, versionId);
        return Response.ok(schema).build();
    }

    @PUT
    public Response saveSchema(
        @PathParam("templateId") Long templateId,
        @PathParam("versionId") Long versionId,
        SchemaSaveDTO dto
    ) {
        SchemaDTO schema = schemaService.saveSchema(templateId, versionId, dto);
        return Response.ok(schema).build();
    }
}

package com.contract.adapter.jaxrs;

import com.contract.adapter.req.TemplateCreateReq;
import com.contract.adapter.rsp.TemplateCreateRsp;
import com.contract.adapter.rsp.TemplateGetRsp;
import com.contract.adapter.rsp.TemplateListRsp;
import com.contract.domain.template.Template;
import com.contract.domain.template.service.TemplateDomainService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class TemplateResource {

    private final TemplateDomainService templateDomainService;

    @POST
    public Response create(TemplateCreateReq req) {
        Template template = templateDomainService.createTemplate(
            req.getTemplateCode(),
            req.getTemplateName(),
            req.getTemplateDesc(),
            req.getBizType()
        );
        TemplateCreateRsp rsp = TemplateCreateRsp.builder()
            .id(template.getIdValue())
            .templateCode(template.getTemplateCodeValue())
            .templateName(template.getTemplateNameValue())
            .status(template.getStatus().name())
            .build();
        return Response.status(Response.Status.CREATED).entity(rsp).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Template t = templateDomainService.getById(id);
        TemplateGetRsp rsp = toGetRsp(t);
        return Response.ok(rsp).build();
    }

    @GET
    public Response listAll() {
        List<Template> templates = templateDomainService.listAll();
        List<TemplateGetRsp> items = templates.stream().map(this::toGetRsp).toList();
        TemplateListRsp rsp = TemplateListRsp.builder()
            .templates(items)
            .total(items.size())
            .build();
        return Response.ok(rsp).build();
    }

    @POST
    @Path("/{id}/disable")
    public Response disable(@PathParam("id") Long id) {
        templateDomainService.disable(id);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/enable")
    public Response enable(@PathParam("id") Long id) {
        templateDomainService.enable(id);
        return Response.ok().build();
    }

    private TemplateGetRsp toGetRsp(Template t) {
        return TemplateGetRsp.builder()
            .id(t.getIdValue())
            .templateCode(t.getTemplateCodeValue())
            .templateName(t.getTemplateNameValue())
            .templateDesc(t.getTemplateDescValue())
            .bizType(t.getBizTypeValue())
            .status(t.getStatus().name())
            .currentVersionId(t.getCurrentVersionId())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }
}

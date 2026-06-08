package com.contract.application.template.convert;

import com.contract.application.template.dto.ActionConfigCreateRequest;
import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigUpdateRequest;
import com.contract.domain.template.ActionConfig;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T23:02:37+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class ActionConfigConverterImpl implements ActionConfigConverter {

    @Override
    public ActionConfigDTO toDTO(ActionConfig domain) {
        if ( domain == null ) {
            return null;
        }

        ActionConfigDTO.ActionConfigDTOBuilder actionConfigDTO = ActionConfigDTO.builder();

        actionConfigDTO.id( domain.getId() );
        actionConfigDTO.templateId( domain.getTemplateId() );
        actionConfigDTO.templateVersionId( domain.getTemplateVersionId() );
        actionConfigDTO.actionCode( domain.getActionCode() );
        actionConfigDTO.actionName( domain.getActionName() );
        actionConfigDTO.actionType( domain.getActionType() );
        actionConfigDTO.bindNodeId( domain.getBindNodeId() );
        actionConfigDTO.bindQueryId( domain.getBindQueryId() );
        actionConfigDTO.confirmRequired( domain.getConfirmRequired() );
        actionConfigDTO.confirmText( domain.getConfirmText() );
        actionConfigDTO.beforeRule( domain.getBeforeRule() );
        actionConfigDTO.afterRule( domain.getAfterRule() );
        actionConfigDTO.propsJson( domain.getPropsJson() );
        actionConfigDTO.sortNo( domain.getSortNo() );
        actionConfigDTO.createdAt( domain.getCreatedAt() );
        actionConfigDTO.updatedAt( domain.getUpdatedAt() );

        return actionConfigDTO.build();
    }

    @Override
    public List<ActionConfigDTO> toDTOList(List<ActionConfig> domains) {
        if ( domains == null ) {
            return null;
        }

        List<ActionConfigDTO> list = new ArrayList<ActionConfigDTO>( domains.size() );
        for ( ActionConfig actionConfig : domains ) {
            list.add( toDTO( actionConfig ) );
        }

        return list;
    }

    @Override
    public ActionConfig toDomain(ActionConfigCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        ActionConfig.ActionConfigBuilder actionConfig = ActionConfig.builder();

        actionConfig.actionName( request.getActionName() );
        actionConfig.actionType( request.getActionType() );
        actionConfig.bindNodeId( request.getBindNodeId() );
        actionConfig.bindQueryId( request.getBindQueryId() );
        actionConfig.confirmRequired( request.getConfirmRequired() );
        actionConfig.confirmText( request.getConfirmText() );
        actionConfig.beforeRule( request.getBeforeRule() );
        actionConfig.afterRule( request.getAfterRule() );
        actionConfig.propsJson( request.getPropsJson() );
        actionConfig.sortNo( request.getSortNo() );

        return actionConfig.build();
    }

    @Override
    public void updateFromDTO(ActionConfigUpdateRequest request, ActionConfig domain) {
        if ( request == null ) {
            return;
        }

        if ( request.getActionName() != null ) {
            domain.setActionName( request.getActionName() );
        }
        if ( request.getBindQueryId() != null ) {
            domain.setBindQueryId( request.getBindQueryId() );
        }
        if ( request.getConfirmRequired() != null ) {
            domain.setConfirmRequired( request.getConfirmRequired() );
        }
        if ( request.getConfirmText() != null ) {
            domain.setConfirmText( request.getConfirmText() );
        }
        if ( request.getBeforeRule() != null ) {
            domain.setBeforeRule( request.getBeforeRule() );
        }
        if ( request.getAfterRule() != null ) {
            domain.setAfterRule( request.getAfterRule() );
        }
        if ( request.getPropsJson() != null ) {
            domain.setPropsJson( request.getPropsJson() );
        }
        if ( request.getSortNo() != null ) {
            domain.setSortNo( request.getSortNo() );
        }
    }
}

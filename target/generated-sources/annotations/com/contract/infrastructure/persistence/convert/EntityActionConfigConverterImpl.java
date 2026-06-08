package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.ActionConfig;
import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T22:45:15+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityActionConfigConverterImpl implements EntityActionConfigConverter {

    @Override
    public ActionConfig toDomain(ActionConfigEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ActionConfig.ActionConfigBuilder actionConfig = ActionConfig.builder();

        actionConfig.id( entity.getId() );
        actionConfig.templateId( entity.getTemplateId() );
        actionConfig.templateVersionId( entity.getTemplateVersionId() );
        actionConfig.actionCode( entity.getActionCode() );
        actionConfig.actionName( entity.getActionName() );
        actionConfig.actionType( entity.getActionType() );
        actionConfig.bindNodeId( entity.getBindNodeId() );
        actionConfig.bindQueryId( entity.getBindQueryId() );
        actionConfig.confirmRequired( entity.getConfirmRequired() );
        actionConfig.confirmText( entity.getConfirmText() );
        actionConfig.beforeRule( entity.getBeforeRule() );
        actionConfig.afterRule( entity.getAfterRule() );
        actionConfig.propsJson( entity.getPropsJson() );
        actionConfig.sortNo( entity.getSortNo() );
        actionConfig.createdAt( entity.getCreatedAt() );
        actionConfig.updatedAt( entity.getUpdatedAt() );
        actionConfig.isDeleted( entity.getIsDeleted() );

        return actionConfig.build();
    }

    @Override
    public ActionConfigEntity toEntity(ActionConfig domain) {
        if ( domain == null ) {
            return null;
        }

        ActionConfigEntity actionConfigEntity = new ActionConfigEntity();

        actionConfigEntity.setId( domain.getId() );
        actionConfigEntity.setTemplateId( domain.getTemplateId() );
        actionConfigEntity.setTemplateVersionId( domain.getTemplateVersionId() );
        actionConfigEntity.setActionCode( domain.getActionCode() );
        actionConfigEntity.setActionName( domain.getActionName() );
        actionConfigEntity.setActionType( domain.getActionType() );
        actionConfigEntity.setBindNodeId( domain.getBindNodeId() );
        actionConfigEntity.setBindQueryId( domain.getBindQueryId() );
        actionConfigEntity.setConfirmRequired( domain.getConfirmRequired() );
        actionConfigEntity.setConfirmText( domain.getConfirmText() );
        actionConfigEntity.setBeforeRule( domain.getBeforeRule() );
        actionConfigEntity.setAfterRule( domain.getAfterRule() );
        actionConfigEntity.setPropsJson( domain.getPropsJson() );
        actionConfigEntity.setSortNo( domain.getSortNo() );
        actionConfigEntity.setCreatedAt( domain.getCreatedAt() );
        actionConfigEntity.setUpdatedAt( domain.getUpdatedAt() );
        actionConfigEntity.setIsDeleted( domain.getIsDeleted() );

        return actionConfigEntity;
    }

    @Override
    public List<ActionConfig> toDomainList(List<ActionConfigEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ActionConfig> list = new ArrayList<ActionConfig>( entities.size() );
        for ( ActionConfigEntity actionConfigEntity : entities ) {
            list.add( toDomain( actionConfigEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDomain(ActionConfig domain, ActionConfigEntity entity) {
        if ( domain == null ) {
            return;
        }

        if ( domain.getTemplateId() != null ) {
            entity.setTemplateId( domain.getTemplateId() );
        }
        if ( domain.getTemplateVersionId() != null ) {
            entity.setTemplateVersionId( domain.getTemplateVersionId() );
        }
        if ( domain.getActionCode() != null ) {
            entity.setActionCode( domain.getActionCode() );
        }
        if ( domain.getActionName() != null ) {
            entity.setActionName( domain.getActionName() );
        }
        if ( domain.getActionType() != null ) {
            entity.setActionType( domain.getActionType() );
        }
        if ( domain.getBindNodeId() != null ) {
            entity.setBindNodeId( domain.getBindNodeId() );
        }
        if ( domain.getBindQueryId() != null ) {
            entity.setBindQueryId( domain.getBindQueryId() );
        }
        if ( domain.getConfirmRequired() != null ) {
            entity.setConfirmRequired( domain.getConfirmRequired() );
        }
        if ( domain.getConfirmText() != null ) {
            entity.setConfirmText( domain.getConfirmText() );
        }
        if ( domain.getBeforeRule() != null ) {
            entity.setBeforeRule( domain.getBeforeRule() );
        }
        if ( domain.getAfterRule() != null ) {
            entity.setAfterRule( domain.getAfterRule() );
        }
        if ( domain.getPropsJson() != null ) {
            entity.setPropsJson( domain.getPropsJson() );
        }
        if ( domain.getSortNo() != null ) {
            entity.setSortNo( domain.getSortNo() );
        }
        if ( domain.getUpdatedAt() != null ) {
            entity.setUpdatedAt( domain.getUpdatedAt() );
        }
        if ( domain.getIsDeleted() != null ) {
            entity.setIsDeleted( domain.getIsDeleted() );
        }
    }
}

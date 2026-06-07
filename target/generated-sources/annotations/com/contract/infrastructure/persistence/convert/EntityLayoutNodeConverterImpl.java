package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.LayoutNode;
import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T02:17:51+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityLayoutNodeConverterImpl implements EntityLayoutNodeConverter {

    @Override
    public LayoutNode toDomain(LayoutNodeEntity entity) {
        if ( entity == null ) {
            return null;
        }

        LayoutNode.LayoutNodeBuilder layoutNode = LayoutNode.builder();

        layoutNode.id( entity.getId() );
        layoutNode.templateId( entity.getTemplateId() );
        layoutNode.templateVersionId( entity.getTemplateVersionId() );
        layoutNode.parentId( entity.getParentId() );
        layoutNode.nodeCode( entity.getNodeCode() );
        layoutNode.nodeName( entity.getNodeName() );
        layoutNode.nodeType( entity.getNodeType() );
        layoutNode.sortNo( entity.getSortNo() );
        layoutNode.levelNo( entity.getLevelNo() );
        layoutNode.nodePath( entity.getNodePath() );
        layoutNode.gridX( entity.getGridX() );
        layoutNode.gridY( entity.getGridY() );
        layoutNode.gridW( entity.getGridW() );
        layoutNode.gridH( entity.getGridH() );
        layoutNode.rowNo( entity.getRowNo() );
        layoutNode.colNo( entity.getColNo() );
        layoutNode.colSpan( entity.getColSpan() );
        layoutNode.rowSpan( entity.getRowSpan() );
        layoutNode.bindType( entity.getBindType() );
        layoutNode.bindRefId( entity.getBindRefId() );
        layoutNode.visibleRule( entity.getVisibleRule() );
        layoutNode.readonlyRule( entity.getReadonlyRule() );
        layoutNode.propsJson( entity.getPropsJson() );
        layoutNode.createdAt( entity.getCreatedAt() );
        layoutNode.updatedAt( entity.getUpdatedAt() );
        layoutNode.isDeleted( entity.getIsDeleted() );

        return layoutNode.build();
    }

    @Override
    public LayoutNodeEntity toEntity(LayoutNode domain) {
        if ( domain == null ) {
            return null;
        }

        LayoutNodeEntity layoutNodeEntity = new LayoutNodeEntity();

        layoutNodeEntity.setId( domain.getId() );
        layoutNodeEntity.setTemplateId( domain.getTemplateId() );
        layoutNodeEntity.setTemplateVersionId( domain.getTemplateVersionId() );
        layoutNodeEntity.setParentId( domain.getParentId() );
        layoutNodeEntity.setNodeCode( domain.getNodeCode() );
        layoutNodeEntity.setNodeName( domain.getNodeName() );
        layoutNodeEntity.setNodeType( domain.getNodeType() );
        layoutNodeEntity.setSortNo( domain.getSortNo() );
        layoutNodeEntity.setLevelNo( domain.getLevelNo() );
        layoutNodeEntity.setNodePath( domain.getNodePath() );
        layoutNodeEntity.setGridX( domain.getGridX() );
        layoutNodeEntity.setGridY( domain.getGridY() );
        layoutNodeEntity.setGridW( domain.getGridW() );
        layoutNodeEntity.setGridH( domain.getGridH() );
        layoutNodeEntity.setRowNo( domain.getRowNo() );
        layoutNodeEntity.setColNo( domain.getColNo() );
        layoutNodeEntity.setColSpan( domain.getColSpan() );
        layoutNodeEntity.setRowSpan( domain.getRowSpan() );
        layoutNodeEntity.setBindType( domain.getBindType() );
        layoutNodeEntity.setBindRefId( domain.getBindRefId() );
        layoutNodeEntity.setVisibleRule( domain.getVisibleRule() );
        layoutNodeEntity.setReadonlyRule( domain.getReadonlyRule() );
        layoutNodeEntity.setPropsJson( domain.getPropsJson() );
        layoutNodeEntity.setCreatedAt( domain.getCreatedAt() );
        layoutNodeEntity.setUpdatedAt( domain.getUpdatedAt() );
        layoutNodeEntity.setIsDeleted( domain.getIsDeleted() );

        return layoutNodeEntity;
    }

    @Override
    public List<LayoutNode> toDomainList(List<LayoutNodeEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<LayoutNode> list = new ArrayList<LayoutNode>( entities.size() );
        for ( LayoutNodeEntity layoutNodeEntity : entities ) {
            list.add( toDomain( layoutNodeEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDomain(LayoutNode domain, LayoutNodeEntity entity) {
        if ( domain == null ) {
            return;
        }

        if ( domain.getTemplateId() != null ) {
            entity.setTemplateId( domain.getTemplateId() );
        }
        if ( domain.getTemplateVersionId() != null ) {
            entity.setTemplateVersionId( domain.getTemplateVersionId() );
        }
        if ( domain.getParentId() != null ) {
            entity.setParentId( domain.getParentId() );
        }
        if ( domain.getNodeCode() != null ) {
            entity.setNodeCode( domain.getNodeCode() );
        }
        if ( domain.getNodeName() != null ) {
            entity.setNodeName( domain.getNodeName() );
        }
        if ( domain.getNodeType() != null ) {
            entity.setNodeType( domain.getNodeType() );
        }
        if ( domain.getSortNo() != null ) {
            entity.setSortNo( domain.getSortNo() );
        }
        if ( domain.getLevelNo() != null ) {
            entity.setLevelNo( domain.getLevelNo() );
        }
        if ( domain.getNodePath() != null ) {
            entity.setNodePath( domain.getNodePath() );
        }
        if ( domain.getGridX() != null ) {
            entity.setGridX( domain.getGridX() );
        }
        if ( domain.getGridY() != null ) {
            entity.setGridY( domain.getGridY() );
        }
        if ( domain.getGridW() != null ) {
            entity.setGridW( domain.getGridW() );
        }
        if ( domain.getGridH() != null ) {
            entity.setGridH( domain.getGridH() );
        }
        if ( domain.getRowNo() != null ) {
            entity.setRowNo( domain.getRowNo() );
        }
        if ( domain.getColNo() != null ) {
            entity.setColNo( domain.getColNo() );
        }
        if ( domain.getColSpan() != null ) {
            entity.setColSpan( domain.getColSpan() );
        }
        if ( domain.getRowSpan() != null ) {
            entity.setRowSpan( domain.getRowSpan() );
        }
        if ( domain.getBindType() != null ) {
            entity.setBindType( domain.getBindType() );
        }
        if ( domain.getBindRefId() != null ) {
            entity.setBindRefId( domain.getBindRefId() );
        }
        if ( domain.getVisibleRule() != null ) {
            entity.setVisibleRule( domain.getVisibleRule() );
        }
        if ( domain.getReadonlyRule() != null ) {
            entity.setReadonlyRule( domain.getReadonlyRule() );
        }
        if ( domain.getPropsJson() != null ) {
            entity.setPropsJson( domain.getPropsJson() );
        }
        if ( domain.getUpdatedAt() != null ) {
            entity.setUpdatedAt( domain.getUpdatedAt() );
        }
        if ( domain.getIsDeleted() != null ) {
            entity.setIsDeleted( domain.getIsDeleted() );
        }
    }
}

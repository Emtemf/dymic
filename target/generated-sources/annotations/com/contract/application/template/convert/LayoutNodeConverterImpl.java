package com.contract.application.template.convert;

import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.domain.template.LayoutNode;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-06T23:27:54+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class LayoutNodeConverterImpl implements LayoutNodeConverter {

    @Override
    public LayoutNodeDTO toDTO(LayoutNode domain) {
        if ( domain == null ) {
            return null;
        }

        LayoutNodeDTO.LayoutNodeDTOBuilder layoutNodeDTO = LayoutNodeDTO.builder();

        layoutNodeDTO.id( domain.getId() );
        layoutNodeDTO.templateId( domain.getTemplateId() );
        layoutNodeDTO.templateVersionId( domain.getTemplateVersionId() );
        layoutNodeDTO.parentId( domain.getParentId() );
        layoutNodeDTO.nodeCode( domain.getNodeCode() );
        layoutNodeDTO.nodeName( domain.getNodeName() );
        layoutNodeDTO.nodeType( domain.getNodeType() );
        layoutNodeDTO.sortNo( domain.getSortNo() );
        layoutNodeDTO.levelNo( domain.getLevelNo() );
        layoutNodeDTO.nodePath( domain.getNodePath() );
        layoutNodeDTO.gridX( domain.getGridX() );
        layoutNodeDTO.gridY( domain.getGridY() );
        layoutNodeDTO.gridW( domain.getGridW() );
        layoutNodeDTO.gridH( domain.getGridH() );
        layoutNodeDTO.rowNo( domain.getRowNo() );
        layoutNodeDTO.colNo( domain.getColNo() );
        layoutNodeDTO.colSpan( domain.getColSpan() );
        layoutNodeDTO.rowSpan( domain.getRowSpan() );
        layoutNodeDTO.bindType( domain.getBindType() );
        layoutNodeDTO.bindRefId( domain.getBindRefId() );
        layoutNodeDTO.visibleRule( domain.getVisibleRule() );
        layoutNodeDTO.readonlyRule( domain.getReadonlyRule() );
        layoutNodeDTO.propsJson( domain.getPropsJson() );

        return layoutNodeDTO.build();
    }

    @Override
    public List<LayoutNodeDTO> toDTOList(List<LayoutNode> domains) {
        if ( domains == null ) {
            return null;
        }

        List<LayoutNodeDTO> list = new ArrayList<LayoutNodeDTO>( domains.size() );
        for ( LayoutNode layoutNode : domains ) {
            list.add( toDTO( layoutNode ) );
        }

        return list;
    }

    @Override
    public LayoutNode toDomain(LayoutNodeCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        LayoutNode.LayoutNodeBuilder layoutNode = LayoutNode.builder();

        layoutNode.parentId( dto.getParentId() );
        layoutNode.nodeType( dto.getNodeType() );
        layoutNode.sortNo( dto.getSortNo() );
        layoutNode.levelNo( dto.getLevelNo() );
        layoutNode.gridX( dto.getGridX() );
        layoutNode.gridY( dto.getGridY() );
        layoutNode.gridW( dto.getGridW() );
        layoutNode.gridH( dto.getGridH() );
        layoutNode.rowNo( dto.getRowNo() );
        layoutNode.colNo( dto.getColNo() );
        layoutNode.colSpan( dto.getColSpan() );
        layoutNode.rowSpan( dto.getRowSpan() );
        layoutNode.bindType( dto.getBindType() );
        layoutNode.bindRefId( dto.getBindRefId() );
        layoutNode.visibleRule( dto.getVisibleRule() );
        layoutNode.readonlyRule( dto.getReadonlyRule() );
        layoutNode.propsJson( dto.getPropsJson() );

        return layoutNode.build();
    }

    @Override
    public void updateDomainFromDTO(LayoutNodeUpdateDTO dto, LayoutNode domain) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getNodeName() != null ) {
            domain.setNodeName( dto.getNodeName() );
        }
        if ( dto.getSortNo() != null ) {
            domain.setSortNo( dto.getSortNo() );
        }
        if ( dto.getGridX() != null ) {
            domain.setGridX( dto.getGridX() );
        }
        if ( dto.getGridY() != null ) {
            domain.setGridY( dto.getGridY() );
        }
        if ( dto.getGridW() != null ) {
            domain.setGridW( dto.getGridW() );
        }
        if ( dto.getGridH() != null ) {
            domain.setGridH( dto.getGridH() );
        }
        if ( dto.getRowNo() != null ) {
            domain.setRowNo( dto.getRowNo() );
        }
        if ( dto.getColNo() != null ) {
            domain.setColNo( dto.getColNo() );
        }
        if ( dto.getColSpan() != null ) {
            domain.setColSpan( dto.getColSpan() );
        }
        if ( dto.getRowSpan() != null ) {
            domain.setRowSpan( dto.getRowSpan() );
        }
        if ( dto.getVisibleRule() != null ) {
            domain.setVisibleRule( dto.getVisibleRule() );
        }
        if ( dto.getReadonlyRule() != null ) {
            domain.setReadonlyRule( dto.getReadonlyRule() );
        }
        if ( dto.getPropsJson() != null ) {
            domain.setPropsJson( dto.getPropsJson() );
        }
    }
}

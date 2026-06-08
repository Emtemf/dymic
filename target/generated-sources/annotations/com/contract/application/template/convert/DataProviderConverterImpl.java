package com.contract.application.template.convert;

import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.domain.template.DataProvider;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T22:45:16+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class DataProviderConverterImpl implements DataProviderConverter {

    @Override
    public DataProviderDTO toDTO(DataProvider domain) {
        if ( domain == null ) {
            return null;
        }

        DataProviderDTO.DataProviderDTOBuilder dataProviderDTO = DataProviderDTO.builder();

        dataProviderDTO.id( domain.getId() );
        dataProviderDTO.providerCode( domain.getProviderCode() );
        dataProviderDTO.providerName( domain.getProviderName() );
        dataProviderDTO.providerType( domain.getProviderType() );
        dataProviderDTO.configJson( domain.getConfigJson() );
        dataProviderDTO.cacheEnabled( domain.getCacheEnabled() );
        dataProviderDTO.cacheTtlSeconds( domain.getCacheTtlSeconds() );
        dataProviderDTO.isTemporary( domain.getIsTemporary() );
        dataProviderDTO.status( domain.getStatus() );

        return dataProviderDTO.build();
    }

    @Override
    public DataProvider toDomain(DataProviderCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        DataProvider.DataProviderBuilder dataProvider = DataProvider.builder();

        dataProvider.providerCode( dto.getProviderCode() );
        dataProvider.providerName( dto.getProviderName() );
        dataProvider.providerType( dto.getProviderType() );
        dataProvider.configJson( dto.getConfigJson() );
        dataProvider.cacheEnabled( dto.getCacheEnabled() );
        dataProvider.cacheTtlSeconds( dto.getCacheTtlSeconds() );

        dataProvider.isTemporary( 0 );

        return dataProvider.build();
    }

    @Override
    public void updateDomainFromDTO(DataProviderUpdateDTO dto, DataProvider domain) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getProviderName() != null ) {
            domain.setProviderName( dto.getProviderName() );
        }
        if ( dto.getConfigJson() != null ) {
            domain.setConfigJson( dto.getConfigJson() );
        }
        if ( dto.getCacheEnabled() != null ) {
            domain.setCacheEnabled( dto.getCacheEnabled() );
        }
        if ( dto.getCacheTtlSeconds() != null ) {
            domain.setCacheTtlSeconds( dto.getCacheTtlSeconds() );
        }
    }

    @Override
    public List<DataProviderDTO> toDTOList(List<DataProvider> domains) {
        if ( domains == null ) {
            return null;
        }

        List<DataProviderDTO> list = new ArrayList<DataProviderDTO>( domains.size() );
        for ( DataProvider dataProvider : domains ) {
            list.add( toDTO( dataProvider ) );
        }

        return list;
    }
}

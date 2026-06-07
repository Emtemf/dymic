package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.DataProvider;
import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T01:27:24+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityDataProviderConverterImpl implements EntityDataProviderConverter {

    @Override
    public DataProvider toDomain(DataProviderEntity entity) {
        if ( entity == null ) {
            return null;
        }

        DataProvider.DataProviderBuilder dataProvider = DataProvider.builder();

        dataProvider.id( entity.getId() );
        dataProvider.providerCode( entity.getProviderCode() );
        dataProvider.providerName( entity.getProviderName() );
        dataProvider.providerType( entity.getProviderType() );
        dataProvider.configJson( entity.getConfigJson() );
        dataProvider.cacheEnabled( entity.getCacheEnabled() );
        dataProvider.cacheTtlSeconds( entity.getCacheTtlSeconds() );
        dataProvider.isTemporary( entity.getIsTemporary() );
        dataProvider.status( entity.getStatus() );
        dataProvider.createdBy( entity.getCreatedBy() );
        dataProvider.createdName( entity.getCreatedName() );
        dataProvider.createdAt( entity.getCreatedAt() );
        dataProvider.updatedBy( entity.getUpdatedBy() );
        dataProvider.updatedName( entity.getUpdatedName() );
        dataProvider.updatedAt( entity.getUpdatedAt() );
        dataProvider.isDeleted( entity.getIsDeleted() );

        return dataProvider.build();
    }

    @Override
    public DataProviderEntity toEntity(DataProvider domain) {
        if ( domain == null ) {
            return null;
        }

        DataProviderEntity dataProviderEntity = new DataProviderEntity();

        dataProviderEntity.setId( domain.getId() );
        dataProviderEntity.setProviderCode( domain.getProviderCode() );
        dataProviderEntity.setProviderName( domain.getProviderName() );
        dataProviderEntity.setProviderType( domain.getProviderType() );
        dataProviderEntity.setConfigJson( domain.getConfigJson() );
        dataProviderEntity.setCacheEnabled( domain.getCacheEnabled() );
        dataProviderEntity.setCacheTtlSeconds( domain.getCacheTtlSeconds() );
        dataProviderEntity.setIsTemporary( domain.getIsTemporary() );
        dataProviderEntity.setStatus( domain.getStatus() );
        dataProviderEntity.setCreatedBy( domain.getCreatedBy() );
        dataProviderEntity.setCreatedName( domain.getCreatedName() );
        dataProviderEntity.setCreatedAt( domain.getCreatedAt() );
        dataProviderEntity.setUpdatedBy( domain.getUpdatedBy() );
        dataProviderEntity.setUpdatedName( domain.getUpdatedName() );
        dataProviderEntity.setUpdatedAt( domain.getUpdatedAt() );
        dataProviderEntity.setIsDeleted( domain.getIsDeleted() );

        return dataProviderEntity;
    }

    @Override
    public List<DataProvider> toDomainList(List<DataProviderEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<DataProvider> list = new ArrayList<DataProvider>( entities.size() );
        for ( DataProviderEntity dataProviderEntity : entities ) {
            list.add( toDomain( dataProviderEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDomain(DataProvider domain, DataProviderEntity entity) {
        if ( domain == null ) {
            return;
        }

        if ( domain.getProviderCode() != null ) {
            entity.setProviderCode( domain.getProviderCode() );
        }
        if ( domain.getProviderName() != null ) {
            entity.setProviderName( domain.getProviderName() );
        }
        if ( domain.getProviderType() != null ) {
            entity.setProviderType( domain.getProviderType() );
        }
        if ( domain.getConfigJson() != null ) {
            entity.setConfigJson( domain.getConfigJson() );
        }
        if ( domain.getCacheEnabled() != null ) {
            entity.setCacheEnabled( domain.getCacheEnabled() );
        }
        if ( domain.getCacheTtlSeconds() != null ) {
            entity.setCacheTtlSeconds( domain.getCacheTtlSeconds() );
        }
        if ( domain.getIsTemporary() != null ) {
            entity.setIsTemporary( domain.getIsTemporary() );
        }
        if ( domain.getStatus() != null ) {
            entity.setStatus( domain.getStatus() );
        }
        if ( domain.getCreatedBy() != null ) {
            entity.setCreatedBy( domain.getCreatedBy() );
        }
        if ( domain.getCreatedName() != null ) {
            entity.setCreatedName( domain.getCreatedName() );
        }
        if ( domain.getUpdatedBy() != null ) {
            entity.setUpdatedBy( domain.getUpdatedBy() );
        }
        if ( domain.getUpdatedName() != null ) {
            entity.setUpdatedName( domain.getUpdatedName() );
        }
        if ( domain.getUpdatedAt() != null ) {
            entity.setUpdatedAt( domain.getUpdatedAt() );
        }
    }
}

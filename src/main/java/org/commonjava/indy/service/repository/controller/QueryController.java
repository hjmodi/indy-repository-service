/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.controller;

import org.apache.commons.lang3.StringUtils;
import org.commonjava.indy.service.repository.data.ArtifactStoreQuery;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.commonjava.indy.service.repository.model.StoreKey.fromString;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.commonjava.indy.service.repository.model.pkg.PackageTypeConstants.isValidPackageType;

@ApplicationScoped
public class QueryController
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Inject
    StoreDataManager storeManager;

    @Inject
    QueryController( final StoreDataManager storeManager )
    {
        this.storeManager = storeManager;
    }

    public List<ArtifactStore> getAllArtifactStores( final String packageType, final String types,
                                                     final String enabled )
            throws IndyWorkflowException
    {
        List<StoreType> typesLs = new ArrayList<>();
        if ( StringUtils.isNotBlank( types ) )
        {
            typesLs = Arrays.stream( types.split( "," ) )
                            .map( String::trim )
                            .filter( t -> StoreType.get( t ) != null )
                            .map( StoreType::get )
                            .collect( Collectors.toList() );
        }
        final List<StoreType> typeList = typesLs;
        return generateQueryResult( () -> {
            Set<ArtifactStore> stores = Collections.emptySet();
            if ( isValidPackageType( packageType ) && typeList.size() == 1 )
            {
                // when packageType and type are all unique value, use storeManager.getArtifactStoresByPkgAndType to improve performance
                stores = storeManager.getArtifactStoresByPkgAndType( packageType, typeList.get( 0 ) );
            }
            else if ( !isValidPackageType( packageType ) && typeList.size() == 0 )
            {
                stores = storeManager.getAllArtifactStores();
            }
            if ( !stores.isEmpty() )
            {
                Stream<ArtifactStore> storeStream = stores.stream();
                boolean isEnabled = Boolean.parseBoolean( enabled );
                if ( isEnabled )
                {
                    storeStream = storeStream.filter( store -> !store.isDisabled() );
                }
                return storeStream.collect( Collectors.toList() );
            }
            ArtifactStoreQuery<ArtifactStore> query = storeManager.query().noPackageType();
            if ( isValidPackageType( packageType ) )
            {
                query.packageType( packageType );
            }
            if ( !typeList.isEmpty() )
            {
                query.storeTypes( typeList.toArray( new StoreType[] {} ) );
            }
            boolean isEnabled = Boolean.parseBoolean( enabled );
            if ( isEnabled )
            {
                query.enabledState( true );
            }
            return query.getAll();

        }, "Failed to get all stores" );
    }

    public List<ArtifactStore> getAllByDefaultPackageTypes()
            throws IndyWorkflowException
    {
        return generateQueryResult( () -> storeManager.query().getAllByDefaultPackageTypes(), "Failed to get stores" );
    }

    public ArtifactStore getByName( final String name )
            throws IndyWorkflowException
    {
        return generateQueryResult( () -> {
            final ArtifactStore store = storeManager.query().getByName( name );
            if ( store == null )
            {
                throw new IndyWorkflowException( NOT_FOUND.getStatusCode(), "Store {} not found", name );
            }

            return store;
        }, "Failed to get store for name {}", name );
    }

    public List<Group> getGroupsContaining( final String storeKey, final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        return generateQueryResult( () -> {
            final StoreKey key = validateStoreKey( storeKey );
            if ( enabled != null )
            {
                return new ArrayList<>( storeManager.query().getGroupsContaining( key, isEnabled ) );
            }
            return new ArrayList<>( storeManager.query().getGroupsContaining( key ) );

        }, "Failed to get groups containing {}", storeKey );
    }

    public List<ArtifactStore> getOrderedConcreteStoresInGroup( final String storeKey, final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        logger.debug( "Searching Concrete repos in group {} with enabled {}", storeKey, isEnabled );
        return generateQueryResult( () -> {
            final StoreKey key = validateStoreKey( storeKey );
            if ( key.getType() != StoreType.group )
            {
                throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKey {}: not a group",
                                                 storeKey );
            }
            return new ArrayList<>( storeManager.query()
                                                .getOrderedConcreteStoresInGroup( key.getPackageType(), key.getName(),
                                                                                  isEnabled ) );
        }, "Failed to get stores in group {}", storeKey );
    }

    public List<ArtifactStore> getOrderedStoresInGroup( final String storeKey, final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        return generateQueryResult( () -> {
            final StoreKey key = validateStoreKey( storeKey );
            if ( key.getType() != StoreType.group )
            {
                throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKey {}: not a group",
                                                 storeKey );
            }
            return new ArrayList<>(
                    storeManager.query().getOrderedStoresInGroup( key.getPackageType(), key.getName(), isEnabled ) );
        }, "Failed to get stores in group {}", storeKey );
    }

    public List<RemoteRepository> getAllRemoteRepositories( final String packageType, final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        return generateQueryResult( () -> storeManager.query()
                                                      .getAllRemoteRepositories(
                                                              packageType == null ? MAVEN_PKG_KEY : packageType,
                                                              isEnabled ),
                                    "Failed to get all remote repos for package type {}", packageType );
    }

    public List<HostedRepository> getAllHostedRepositories( final String packageType, final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        return generateQueryResult( () -> storeManager.query()
                                                      .getAllHostedRepositories(
                                                              packageType == null ? MAVEN_PKG_KEY : packageType,
                                                              isEnabled ),
                                    "Failed to get all hosted repos for package type {}", packageType );
    }

    public List<Group> getAllGroups( final String packageType, final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        return generateQueryResult(
                () -> storeManager.query().getAllGroups( packageType == null ? MAVEN_PKG_KEY : packageType, isEnabled ),
                "Failed to get all groups for package type {}", packageType );
    }

    public List<Group> getGroupsAffectedBy( final String[] keys )
            throws IndyWorkflowException
    {
        return generateQueryResult( () -> {
            final Set<StoreKey> storeKeys = new HashSet<>();
            for ( String s : keys )
            {
                StoreKey storeKey = validateStoreKey( s.trim() );
                storeKeys.add( storeKey );
            }
            return new ArrayList<>( storeManager.query().getGroupsAffectedBy( storeKeys ) );
        }, "Failed to get groups affected by keys: {}", (Object[]) keys );
    }

    public List<RemoteRepository> queryRemotesByPackageTypeAndUrl( final String packageType, final String url,
                                                                   final String enabled )
            throws IndyWorkflowException
    {
        final boolean isEnabled =
                enabled == null || enabled.equalsIgnoreCase( "yes" ) || Boolean.parseBoolean( enabled );
        return generateQueryResult( () -> storeManager.query().getRemoteRepositoryByUrl( packageType, url, isEnabled ),
                                    "Failed to get remote repositories for packageType {} with remote url {}",
                                    packageType, url );
    }

    public Boolean isStoreDataEmpty()
    {
        return storeManager.isEmpty();
    }

    private StoreKey validateStoreKey( final String storeKey )
            throws IndyWorkflowException
    {
        if ( storeKey == null )
        {
            throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(),
                                             "Illegal storeKey: store key can not be null" );
        }
        try
        {
            return fromString( storeKey );
        }
        catch ( IllegalArgumentException e )
        {
            throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKey {}", e, storeKey );
        }
    }

    private <R> R generateQueryResult( final QuerySupplier<R> t, final String message, final Object... params )
            throws IndyWorkflowException
    {
        try
        {
            return t.get();
        }
        catch ( IndyDataException e )
        {
            final Object[] newParams;
            if ( params != null )
            {
                int length = params.length + 1;
                newParams = new Object[length];
                System.arraycopy( params, 0, newParams, 0, params.length );
                newParams[length - 1] = e.getMessage();
            }
            else
            {
                newParams = new Object[] { e.getMessage() };
            }
            final String newMessage = message + ", Reason: {}";
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(), newMessage, e, newParams );
        }
    }

    private interface QuerySupplier<T>
    {
        T get()
                throws IndyDataException, IndyWorkflowException;
    }

}

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
package org.commonjava.indy.service.repository.data;

import org.commonjava.event.common.EventMetadata;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.audit.ChangeSummary;
import org.commonjava.indy.service.repository.exception.IndyDataException;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Data manager used to access and manipulate the configurations for {@link ArtifactStore} instances.
 * @author jdcasey
 *
 */
public interface StoreDataManager
{

    String EVENT_ORIGIN = "event-origin";

    /**
     * We need to modify the way Indy processes the readonly flag on repositories. It should apply to user operations but 
     * NOT apply to workflow like promotion. Set it to true in EventMetadata to bypass the readonly check.
     */
    String IGNORE_READONLY = "ignore-readonly";

    /**
     * We calculate and pass the affected groups through different modules, e.g, in case of promotion.
     */
    String AFFECTED_GROUPS = "affected_groups";

    /**
     * We pass the target store through different modules, e.g, in case of promotion.
     */
    String TARGET_STORE = "target_store";

    /**
     * Need to store change summary for repository change processing
     */
    String CHANGE_SUMMARY = "change-summary";

    ArtifactStoreQuery<ArtifactStore> query();

    /**
     * Return true if the system contains a {@link ArtifactStore} with the given key (combination of {@link StoreType} and name); false otherwise.
     */
    boolean hasArtifactStore( StoreKey key );

    /**
     * Return the {@link ArtifactStore} instance corresponding to the given key, where key is a composite of {@link StoreType}
     * (hosted, remote, or group) and name.
     */
    Optional<ArtifactStore> getArtifactStore( StoreKey key )
            throws IndyDataException;

    /**
     * Return the full list of {@link ArtifactStore} instances of a given {@link StoreType} (hosted, remote, or group) available on the system.
     */
    Set<ArtifactStore> getAllArtifactStores()
            throws IndyDataException;

    /**
     * Return the {@link ArtifactStore} instances as a {@link Stream}.
     */
    Stream<ArtifactStore> streamArtifactStores()
            throws IndyDataException;

    /**
     * Return a mapping of {@link ArtifactStore}'s keyed by the corresponding {@link StoreKey}.
     * @return -
     */
    Map<StoreKey, ArtifactStore> getArtifactStoresByKey();

    /**
     * Store a modified or new {@link ArtifactStore} instance. If the store already exists, and <code>skipIfExists</code> is true, abort the
     * operation.
     * @param eventMetadata TODO
     */
    boolean storeArtifactStore( ArtifactStore store, final ChangeSummary summary, boolean skipIfExists,
                                boolean fireEvents, EventMetadata eventMetadata )
            throws IndyDataException;

    /**
     * Delete the {@link ArtifactStore} corresponding to the given {@link StoreKey}. If the store doesn't exist, simply return (don't fail).
     * @param eventMetadata TODO
     */
    void deleteArtifactStore( StoreKey key, final ChangeSummary summary, EventMetadata eventMetadata )
            throws IndyDataException;

    /**
     * Delete all {@link ArtifactStore} instances currently in the system.
     */
    void clear( final ChangeSummary summary )
            throws IndyDataException;

    /**
     * If no {@link ArtifactStore}'s exist in the system, install a couple of defaults:
     * <ul>
     * <li>Remote <code>central</code> pointing to the Maven central repository at http://repo.maven.apache.org/maven2/</li>
     * <li>Hosted <code>local-deployments</code> that can host both releases and snapshots</li>
     * <li>Group <code>public</code> containing <code>central</code> and <code>local-deployments</code> as members</li>
     * </ul>
     */
    void install()
            throws IndyDataException;

    /**
     * Mechanism for clearing all cached {@link ArtifactStore} instances and reloading them from some backing store.
     */
    void reload()
            throws IndyDataException;

    /**
     * Return true once any post-construction code runs.
     */
    boolean isStarted();

    /**
     * Check if store is a readonly hosted repository. Return true only when store is a readonly {@link HostedRepository}
     */
    boolean isReadonly( ArtifactStore store );

    boolean isEmpty();

    /**
     * Stream of StoreKey instances present in the system.
     */
    Stream<StoreKey> streamArtifactStoreKeys();

    Set<StoreKey> getStoreKeysByPkg( String pkg );

    Set<StoreKey> getStoreKeysByPkgAndType( final String pkg, final StoreType type );

    Set<Group> affectedBy( Collection<StoreKey> keys )
            throws IndyDataException;

    /**
     * Get affected-by groups from event metadata if provided.
     * @param keys -
     * @param eventMetadata -
     */
    Set<Group> affectedBy( Collection<StoreKey> keys, EventMetadata eventMetadata ) throws IndyDataException;

    Set<ArtifactStore> getArtifactStoresByPkgAndType( String packageType, StoreType storeType );

}

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
package org.commonjava.indy.service.repository.jaxrs.version;

import org.commonjava.indy.service.repository.controller.StatsController;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.jaxrs.ResponseHelper;
import org.commonjava.indy.service.repository.model.dto.EndpointViewListing;
import org.commonjava.indy.service.repository.model.version.Versioning;
import org.commonjava.indy.service.repository.util.JaxRsUriFormatter;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;
import static org.commonjava.indy.service.repository.model.PackageTypes.getPackageTypeDescriptorMap;
import static org.commonjava.indy.service.repository.model.PackageTypes.getPackageTypes;
import static org.commonjava.indy.service.repository.util.Constants.API_PREFIX;

@Tag( name = "Generic Infrastructure Queries (UI Support)",
      description = "Various read-only operations for retrieving information about the system." )
@Path( "/api/stats" )
public class StatsHandler
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Inject
    StatsController statsController;

    @Inject
    ResponseHelper responseHelper;

    @Inject
    JaxRsUriFormatter uriFormatter;

    @Operation( summary = "Retrieve versioning information about this APP instance" )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = Versioning.class ) ),
                  description = "The version info of the APIs" )
    @Path( "/version-info" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAppVersion()
    {
        return responseHelper.formatOkResponseWithJsonEntity( statsController.getVersionInfo() );
    }

    @Operation(
            summary = "Retrieve a mapping of the package type names to descriptors (eg. maven, npm, generic-http, etc) available on the system." )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = Map.class ),
                                                            example = "{\n" + "\"maven\": {\"key\": \"maven\","
                                                                    + "\"contentRestBasePath\": \"/api/content/maven\","
                                                                    + "\"adminRestBasePath\": \"/api/admin/stores/maven\" }}" ),
                  description = "The package type listing of packageType => details" )
    @Path( "/package-type/map" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getPackageTypeMap()
    {
        return ok( getPackageTypeDescriptorMap() ).build();
    }

    @Operation(
            summary = "Retrieve a list of the package type names (eg. maven, npm, generic-http, etc) available on the system." )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = Set.class ),
                                                            example = "[\"generic-http\",\"maven\",\"npm\"]" ),
                  description = "The package type listing" )
    @Path( "/responseCode-type/keys" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getPackageTypeNames()
    {
        return ok( new TreeSet<>( getPackageTypes() ) ).build();
    }

    @Operation(
            summary = "Retrieve a listing of the artifact stores available on the system. This is especially useful for setting up a network of Indy instances that reference one another" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = EndpointViewListing.class ) ),
                  description = "The artifact store listing" )
    @Path( "/all-endpoints" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAllEndpoints( @Context final UriInfo uriInfo )
    {
        Response response;
        try
        {
            final String baseUri = uriInfo.getBaseUriBuilder().path( API_PREFIX ).build().toString();

            final EndpointViewListing listing = statsController.getEndpointsListing( baseUri, uriFormatter );
            response = responseHelper.formatOkResponseWithJsonEntity( listing );

            logger.info( "\n\n\n\n\n\n{} Sent all-endpoints:\n\n{}\n\n\n\n\n\n\n", new Date(), listing );
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to retrieve endpoint listing: %s", responseHelper.formatEntity( e ) ),
                          e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

}

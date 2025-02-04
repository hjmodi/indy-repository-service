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
package org.commonjava.indy.service.repository.ftests.admin;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.AbstractStoreManagementTest;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.commonjava.indy.service.repository.model.Group;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class CreateGroupThenModifyAndVerifyChangesTest
        extends AbstractStoreManagementTest
{

    @Test
    public void addAndModifyGroupThenRetrieveIt()
            throws Exception
    {
        final String name = newName();
        final Group repo = new Group( MAVEN_PKG_KEY, name );
        String json = mapper.writeValueAsString( repo );

        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( repo.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, repo, Group.class ) );

        repo.setDescription( "Testing" );
        json = mapper.writeValueAsString( repo );
        final String repoUrl = getRepoUrl( repo.getKey() );
        given().body( json ).contentType( APPLICATION_JSON ).put( repoUrl ).then().statusCode( OK.getStatusCode() );

        given().get( repoUrl )
               .then()
               .statusCode( OK.getStatusCode() )
               .body( new RepoEqualMatcher<>( mapper, repo, Group.class ) )
               .body( "description", is( repo.getDescription() ) );

    }
}

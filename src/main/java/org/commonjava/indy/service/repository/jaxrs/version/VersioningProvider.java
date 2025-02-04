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

import org.commonjava.indy.service.repository.model.version.DeprecatedApis;
import org.commonjava.indy.service.repository.model.version.Versioning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Producer class that reads a properties file off the classpath containing version info for the APP, and assembles an instance of {@link Versioning},
 * which this component then provides for injecting into other components.
 */
@Singleton
public class VersioningProvider
{

    private static final String INDY_VERSIONING_PROPERTIES = "version.properties";

    private static final String INDY_DEPRECATED_APIS_PROPERTIES = "deprecated-apis.properties";

    private final Versioning versioning;

    private final DeprecatedApis deprecatedApis;

    public VersioningProvider()
    {
        ClassLoader cl = VersioningProvider.class.getClassLoader();

        // Load version
        final Properties props = new Properties();
        Logger logger = LoggerFactory.getLogger( getClass() );
        try (InputStream is = cl.getResourceAsStream( INDY_VERSIONING_PROPERTIES ))
        {
            if ( is != null )
            {
                props.load( is );
            }
            else
            {
                logger.warn( "Resource not found, file: {}, loader: {}", INDY_VERSIONING_PROPERTIES, cl );
            }
        }
        catch ( final IOException e )
        {
            logger.error(
                    "Failed to read Indy versioning information from classpath resource: " + INDY_VERSIONING_PROPERTIES,
                    e );
        }

        versioning =
                new Versioning( props.getProperty( "version", "unknown" ), props.getProperty( "builder", "unknown" ),
                                props.getProperty( "commit.id", "unknown" ),
                                props.getProperty( "timestamp", "unknown" ),
                                props.getProperty( "api-version", "unknown" ) );

        // Load deprecated-apis
        String deprecatedApiFile = System.getProperty( "ENV_DEPRECATED_API_FILE", INDY_DEPRECATED_APIS_PROPERTIES );
        logger.info( "Get deprecatedApiFile: {}", deprecatedApiFile );

        final Properties deprApis = new Properties();
        try (InputStream is = cl.getResourceAsStream( deprecatedApiFile ))
        {
            if ( is != null )
            {
                deprApis.load( is );
            }
            else
            {
                logger.warn( "Resource not found, file: {}, loader: {}", INDY_DEPRECATED_APIS_PROPERTIES, cl );
            }
        }
        catch ( final IOException e )
        {
            logger.error(
                    "Failed to read Indy deprecated api information from classpath resource: " + deprecatedApiFile, e );
        }

        deprecatedApis = new DeprecatedApis( deprApis );

    }

    @Produces
    @Default
    public Versioning getVersioningInstance()
    {
        return versioning;
    }

    @Produces
    @Default
    public DeprecatedApis getDeprecatedApis()
    {
        return deprecatedApis;
    }

}

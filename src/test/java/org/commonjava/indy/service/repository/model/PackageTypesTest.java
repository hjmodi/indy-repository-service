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
package org.commonjava.indy.service.repository.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.commonjava.indy.service.repository.model.pkg.PackageTypeConstants.PKG_TYPE_GENERIC_HTTP;
import static org.commonjava.indy.service.repository.model.pkg.PackageTypeConstants.PKG_TYPE_MAVEN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by jdcasey on 5/12/17.
 */
public class PackageTypesTest
{
    @Test
    public void loadMavenAndGenericPackageTypes()
    {
        Set<String> typeStrings = PackageTypes.getPackageTypes();
        assertThat( typeStrings.contains( PKG_TYPE_MAVEN ), equalTo( true ) );
        assertThat( typeStrings.contains( PKG_TYPE_GENERIC_HTTP ), equalTo( true ) );
    }
}

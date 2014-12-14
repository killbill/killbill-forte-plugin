/*
 * Copyright 2014 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.forte;

import org.killbill.billing.plugin.TestUtils;
import org.killbill.billing.plugin.forte.client.ForteAGIClient;
import org.testng.annotations.BeforeClass;

public abstract class TestRemoteBase {

    // To run these tests, you need a properties file in the classpath (e.g. src/test/resources/forte.properties)
    // See README.md for details on the required properties
    private static final String PROPERTIES_FILE_NAME = "forte.properties";

    protected ForteAGIClient agiClient;

    @BeforeClass(groups = "slow")
    public void setUpBeforeClass() throws Exception {
        this.agiClient = new ForteAGIClient(TestUtils.loadProperties(PROPERTIES_FILE_NAME));
    }
}

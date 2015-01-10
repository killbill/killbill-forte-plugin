/*
 * Copyright 2015 The Billing Project, LLC
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

package org.killbill.billing.plugin.forte.client;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestForteWSClientAuthentication {

    @Test(groups = "fast")
    public void testGenerateUTCInTicks() {
        final Long utcTimeMillis = 63556506722L;

        final ForteWSClient client = new ForteWSClient(0, "1234", "TOTO", true);
        Assert.assertEquals(client.generateUTCInTicks(utcTimeMillis), "621991533060000000");
    }

    @Test(groups = "fast")
    public void testGenerateTSHASH() {
        final String utcInTicks = "1256921035220000000";

        final ForteWSClient client = new ForteWSClient(0, "1234", "TOTO", true);
        Assert.assertEquals(client.generateTSHash(utcInTicks), "c9fb952e000344fa0ef2ab162eebd0c6");
    }
}

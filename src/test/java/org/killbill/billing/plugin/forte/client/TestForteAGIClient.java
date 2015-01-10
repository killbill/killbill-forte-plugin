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

package org.killbill.billing.plugin.forte.client;

import java.math.BigDecimal;
import java.util.Map;

import org.killbill.billing.plugin.forte.TestRemoteBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestForteAGIClient extends TestRemoteBase {

    private static final BigDecimal AMOUNT = new BigDecimal("10");
    private static final Map<String, Object> OPTIONAL_DATA = null;

    private static final String RESPONSE_TYPE_APPROVAL = "A";
    private static final String RESPONSE_CODE_APPROVED = "A01";
    private static final String RESPONSE_DESCRIPTION_APPROVED = "APPROVED";
    private static final String RESPONSE_DESCRIPTION_TEST_APPROVAL = "TEST APPROVAL";

    @Test(groups = "slow")
    public void testCreditCardSaleTransaction() throws Exception {
        final Map<String, String> saleResponse = agiClient.createSaleTransaction(AMOUNT,
                                                                                 CUSTOMER_FIRST_NAME,
                                                                                 CUSTOMER_LAST_NAME,
                                                                                 CARD_NAME,
                                                                                 CARD_TYPE,
                                                                                 CARD_NUMBER,
                                                                                 CARD_EXP_MONTH,
                                                                                 CARD_EXP_YEAR,
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(saleResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.CREDIT_CARD_SALE, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_TEST_APPROVAL, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST), CUSTOMER_FIRST_NAME, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST), CUSTOMER_LAST_NAME, saleResponse.toString());
    }

    @Test(groups = "slow")
    public void testCreditCardAuthCaptureTransaction() throws Exception {
        final Map<String, String> authResponse = agiClient.createAuthTransaction(AMOUNT,
                                                                                 CUSTOMER_FIRST_NAME,
                                                                                 CUSTOMER_LAST_NAME,
                                                                                 CARD_NAME,
                                                                                 CARD_TYPE,
                                                                                 CARD_NUMBER,
                                                                                 CARD_EXP_MONTH,
                                                                                 CARD_EXP_YEAR,
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_TEST_APPROVAL, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.CREDIT_CARD_AUTH, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST), CUSTOMER_FIRST_NAME, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST), CUSTOMER_LAST_NAME, authResponse.toString());

        final Map<String, String> captureResponse = agiClient.createCaptureTransaction(authResponse.get(ForteAGIClient.PG_TRACE_NUMBER),
                                                                                       authResponse.get(ForteAGIClient.PG_AUTHORIZATION_CODE),
                                                                                       null);
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, captureResponse.toString());
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, captureResponse.toString());
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, captureResponse.toString());
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.CREDIT_CARD_CAPTURE, captureResponse.toString());
    }

    @Test(groups = "slow")
    public void testCreditCardAuthVoidTransaction() throws Exception {
        final Map<String, String> authResponse = agiClient.createAuthTransaction(AMOUNT,
                                                                                 CUSTOMER_FIRST_NAME,
                                                                                 CUSTOMER_LAST_NAME,
                                                                                 CARD_NAME,
                                                                                 CARD_TYPE,
                                                                                 CARD_NUMBER,
                                                                                 CARD_EXP_MONTH,
                                                                                 CARD_EXP_YEAR,
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_TEST_APPROVAL, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.CREDIT_CARD_AUTH, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST), CUSTOMER_FIRST_NAME, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST), CUSTOMER_LAST_NAME, authResponse.toString());

        final Map<String, String> voidResponse = agiClient.createVoidTransaction(authResponse.get(ForteAGIClient.PG_TRACE_NUMBER),
                                                                                 authResponse.get(ForteAGIClient.PG_AUTHORIZATION_CODE),
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, voidResponse.toString());
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, voidResponse.toString());
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, voidResponse.toString());
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.CREDIT_CARD_VOID, voidResponse.toString());
    }

    @Test(groups = "slow")
    public void testEFTSaleTransaction() throws Exception {
        final Map<String, String> saleResponse = agiClient.createSaleTransaction(AMOUNT,
                                                                                 CUSTOMER_FIRST_NAME,
                                                                                 CUSTOMER_LAST_NAME,
                                                                                 TRANSIT_ROUTING_NUMBER,
                                                                                 ACCOUNT_NUMBER,
                                                                                 SAVINGS,
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(saleResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.EFT_SALE, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST), CUSTOMER_FIRST_NAME, saleResponse.toString());
        Assert.assertEquals(saleResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST), CUSTOMER_LAST_NAME, saleResponse.toString());
    }

    @Test(groups = "slow")
    public void testEFTAuthCaptureTransaction() throws Exception {
        final Map<String, String> authResponse = agiClient.createAuthTransaction(AMOUNT,
                                                                                 CUSTOMER_FIRST_NAME,
                                                                                 CUSTOMER_LAST_NAME,
                                                                                 TRANSIT_ROUTING_NUMBER,
                                                                                 ACCOUNT_NUMBER,
                                                                                 SAVINGS,
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.EFT_AUTH, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST), CUSTOMER_FIRST_NAME, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST), CUSTOMER_LAST_NAME, authResponse.toString());

        final Map<String, String> captureResponse = agiClient.createCaptureTransaction(authResponse.get(ForteAGIClient.PG_TRACE_NUMBER),
                                                                                       authResponse.get(ForteAGIClient.PG_AUTHORIZATION_CODE),
                                                                                       OPTIONAL_DATA);
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, captureResponse.toString());
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, captureResponse.toString());
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, captureResponse.toString());
        Assert.assertEquals(captureResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.EFT_CAPTURE, captureResponse.toString());
    }

    @Test(groups = "slow")
    public void testEFTAuthVoidTransaction() throws Exception {
        final Map<String, String> authResponse = agiClient.createAuthTransaction(AMOUNT,
                                                                                 CUSTOMER_FIRST_NAME,
                                                                                 CUSTOMER_LAST_NAME,
                                                                                 TRANSIT_ROUTING_NUMBER,
                                                                                 ACCOUNT_NUMBER,
                                                                                 SAVINGS,
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.EFT_AUTH, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST), CUSTOMER_FIRST_NAME, authResponse.toString());
        Assert.assertEquals(authResponse.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST), CUSTOMER_LAST_NAME, authResponse.toString());

        final Map<String, String> voidResponse = agiClient.createVoidTransaction(authResponse.get(ForteAGIClient.PG_TRACE_NUMBER),
                                                                                 authResponse.get(ForteAGIClient.PG_AUTHORIZATION_CODE),
                                                                                 OPTIONAL_DATA);
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_RESPONSE_TYPE), RESPONSE_TYPE_APPROVAL, voidResponse.toString());
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_RESPONSE_CODE), RESPONSE_CODE_APPROVED, voidResponse.toString());
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION_APPROVED, voidResponse.toString());
        Assert.assertEquals(voidResponse.get(ForteAGIClient.PG_TRANSACTION_TYPE), ForteAGIClient.EFT_VOID, voidResponse.toString());
    }
}

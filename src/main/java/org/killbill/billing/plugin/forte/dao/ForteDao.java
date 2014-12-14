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

package org.killbill.billing.plugin.forte.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.jooq.impl.DSL;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;
import org.killbill.billing.plugin.forte.api.FortePaymentPluginApi;
import org.killbill.billing.plugin.forte.client.ForteAGIClient;
import org.killbill.billing.plugin.forte.dao.gen.tables.FortePaymentMethods;
import org.killbill.billing.plugin.forte.dao.gen.tables.ForteResponses;
import org.killbill.billing.plugin.forte.dao.gen.tables.records.FortePaymentMethodsRecord;
import org.killbill.billing.plugin.forte.dao.gen.tables.records.ForteResponsesRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import static org.killbill.billing.plugin.forte.dao.gen.tables.FortePaymentMethods.FORTE_PAYMENT_METHODS;
import static org.killbill.billing.plugin.forte.dao.gen.tables.ForteResponses.FORTE_RESPONSES;

public class ForteDao extends PluginPaymentDao<ForteResponsesRecord, ForteResponses, FortePaymentMethodsRecord, FortePaymentMethods> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ForteDao(final DataSource dataSource) throws SQLException {
        super(FORTE_RESPONSES, FORTE_PAYMENT_METHODS, dataSource);
    }

    // Responses

    @Override
    public void addResponse(final UUID kbAccountId,
                            final UUID kbPaymentId,
                            final UUID kbPaymentTransactionId,
                            final TransactionType transactionType,
                            final BigDecimal amount,
                            final Currency currency,
                            final Map response,
                            final DateTime utcNow,
                            final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
                new WithConnectionCallback<Void>() {
                    @Override
                    public Void withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, dialect, settings)
                           .insertInto(FORTE_RESPONSES,
                                       FORTE_RESPONSES.KB_ACCOUNT_ID,
                                       FORTE_RESPONSES.KB_PAYMENT_ID,
                                       FORTE_RESPONSES.KB_PAYMENT_TRANSACTION_ID,
                                       FORTE_RESPONSES.TRANSACTION_TYPE,
                                       FORTE_RESPONSES.AMOUNT,
                                       FORTE_RESPONSES.CURRENCY,
                                       FORTE_RESPONSES.PG_MERCHANT_ID,
                                       FORTE_RESPONSES.PG_TRANSACTION_TYPE,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_1,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_2,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_3,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_4,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_5,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_6,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_7,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_8,
                                       FORTE_RESPONSES.PG_MERCHANT_DATA_9,
                                       FORTE_RESPONSES.PG_TOTAL_AMOUNT,
                                       FORTE_RESPONSES.PG_SALES_TAX_AMOUNT,
                                       FORTE_RESPONSES.PG_CUSTOMER_TOKEN,
                                       FORTE_RESPONSES.PG_CLIENT_ID,
                                       FORTE_RESPONSES.PG_CONSUMER_ID,
                                       FORTE_RESPONSES.ECOM_CONSUMERORDERID,
                                       FORTE_RESPONSES.PG_PAYMENT_TOKEN,
                                       FORTE_RESPONSES.PG_PAYMENT_METHOD_ID,
                                       FORTE_RESPONSES.ECOM_WALLETID,
                                       FORTE_RESPONSES.ECOM_BILLTO_POSTAL_NAME_FIRST,
                                       FORTE_RESPONSES.ECOM_BILLTO_POSTAL_NAME_LAST,
                                       FORTE_RESPONSES.PG_BILLTO_POSTAL_NAME_COMPANY,
                                       FORTE_RESPONSES.ECOM_BILLTO_ONLINE_EMAIL,
                                       FORTE_RESPONSES.PG_RESPONSE_TYPE,
                                       FORTE_RESPONSES.PG_RESPONSE_CODE,
                                       FORTE_RESPONSES.PG_RESPONSE_DESCRIPTION,
                                       FORTE_RESPONSES.PG_AVS_RESULT,
                                       FORTE_RESPONSES.PG_TRACE_NUMBER,
                                       FORTE_RESPONSES.PG_AUTHORIZATION_CODE,
                                       FORTE_RESPONSES.PG_PREAUTH_RESULT,
                                       FORTE_RESPONSES.PG_PREAUTH_DESCRIPTION,
                                       FORTE_RESPONSES.PG_PREAUTH_NEG_REPORT,
                                       FORTE_RESPONSES.PG_CVV2_RESULT,
                                       FORTE_RESPONSES.PG_3D_SECURE_RESULT,
                                       FORTE_RESPONSES.PG_AVAILABLE_CARD_BALANCE,
                                       FORTE_RESPONSES.PG_REQUESTED_AMOUNT,
                                       FORTE_RESPONSES.PG_CONVENIENCE_FEE,
                                       FORTE_RESPONSES.ADDITIONAL_DATA,
                                       FORTE_RESPONSES.CREATED_DATE,
                                       FORTE_RESPONSES.KB_TENANT_ID)
                           .values(kbAccountId.toString(),
                                   kbPaymentId.toString(),
                                   kbPaymentTransactionId.toString(),
                                   transactionType.toString(),
                                   amount,
                                   currency,
                                   response.get(ForteAGIClient.PG_MERCHANT_ID),
                                   response.get(ForteAGIClient.PG_TRANSACTION_TYPE),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_1),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_2),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_3),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_4),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_5),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_6),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_7),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_8),
                                   response.get(ForteAGIClient.PG_MERCHANT_DATA_9),
                                   response.get(ForteAGIClient.PG_TOTAL_AMOUNT),
                                   response.get(ForteAGIClient.PG_SALES_TAX_AMOUNT),
                                   response.get(ForteAGIClient.PG_CUSTOMER_TOKEN),
                                   response.get(ForteAGIClient.PG_CLIENT_ID),
                                   response.get(ForteAGIClient.PG_CONSUMER_ID),
                                   response.get(ForteAGIClient.ECOM_CONSUMERORDERID),
                                   response.get(ForteAGIClient.PG_PAYMENT_TOKEN),
                                   response.get(ForteAGIClient.PG_PAYMENT_METHOD_ID),
                                   response.get(ForteAGIClient.ECOM_WALLETID),
                                   response.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST),
                                   response.get(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST),
                                   response.get(ForteAGIClient.PG_BILLTO_POSTAL_NAME_COMPANY),
                                   response.get(ForteAGIClient.ECOM_BILLTO_ONLINE_EMAIL),
                                   response.get(ForteAGIClient.PG_RESPONSE_TYPE),
                                   response.get(ForteAGIClient.PG_RESPONSE_CODE),
                                   response.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION),
                                   response.get(ForteAGIClient.PG_AVS_RESULT),
                                   response.get(ForteAGIClient.PG_TRACE_NUMBER),
                                   response.get(ForteAGIClient.PG_AUTHORIZATION_CODE),
                                   response.get(ForteAGIClient.PG_PREAUTH_RESULT),
                                   response.get(ForteAGIClient.PG_PREAUTH_DESCRIPTION),
                                   response.get(ForteAGIClient.PG_PREAUTH_NEG_REPORT),
                                   response.get(ForteAGIClient.PG_CVV2_RESULT),
                                   response.get(ForteAGIClient.PG_3D_SECURE_RESULT),
                                   response.get(ForteAGIClient.PG_AVAILABLE_CARD_BALANCE),
                                   response.get(ForteAGIClient.PG_REQUESTED_AMOUNT),
                                   response.get(ForteAGIClient.PG_CONVENIENCE_FEE),
                                   null,
                                   toTimestamp(utcNow),
                                   kbTenantId.toString())
                           .execute();
                        return null;
                    }
                });
    }

    // Payment methods

    @Override
    public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final boolean isDefault, final Map properties, final DateTime utcNow, final UUID kbTenantId) throws SQLException {
        final String ccNumber = getProperty(FortePaymentPluginApi.PROPERTY_CC_NUMBER, properties);
        final String ccLast4 = ccNumber == null ? null : ccNumber.substring(ccNumber.length() - 5, ccNumber.length() - 1);

        execute(dataSource.getConnection(),
                new WithConnectionCallback<Void>() {
                    @Override
                    public Void withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, dialect, settings)
                           .insertInto(FORTE_PAYMENT_METHODS,
                                       FORTE_PAYMENT_METHODS.KB_ACCOUNT_ID,
                                       FORTE_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID,
                                       FORTE_PAYMENT_METHODS.TOKEN,
                                       FORTE_PAYMENT_METHODS.CC_FIRST_NAME,
                                       FORTE_PAYMENT_METHODS.CC_LAST_NAME,
                                       FORTE_PAYMENT_METHODS.CC_TYPE,
                                       FORTE_PAYMENT_METHODS.CC_EXP_MONTH,
                                       FORTE_PAYMENT_METHODS.CC_EXP_YEAR,
                                       FORTE_PAYMENT_METHODS.CC_NUMBER,
                                       FORTE_PAYMENT_METHODS.CC_LAST_4,
                                       FORTE_PAYMENT_METHODS.CC_START_MONTH,
                                       FORTE_PAYMENT_METHODS.CC_START_YEAR,
                                       FORTE_PAYMENT_METHODS.CC_ISSUE_NUMBER,
                                       FORTE_PAYMENT_METHODS.CC_VERIFICATION_VALUE,
                                       FORTE_PAYMENT_METHODS.CC_TRACK_DATA,
                                       FORTE_PAYMENT_METHODS.TRANSIT_ROUTING_NUMBER,
                                       FORTE_PAYMENT_METHODS.ACCOUNT_NUMBER,
                                       FORTE_PAYMENT_METHODS.ACCOUNT_TYPE,
                                       FORTE_PAYMENT_METHODS.ADDRESS1,
                                       FORTE_PAYMENT_METHODS.ADDRESS2,
                                       FORTE_PAYMENT_METHODS.CITY,
                                       FORTE_PAYMENT_METHODS.STATE,
                                       FORTE_PAYMENT_METHODS.ZIP,
                                       FORTE_PAYMENT_METHODS.COUNTRY,
                                       FORTE_PAYMENT_METHODS.IS_DEFAULT,
                                       FORTE_PAYMENT_METHODS.IS_DELETED,
                                       FORTE_PAYMENT_METHODS.ADDITIONAL_DATA,
                                       FORTE_PAYMENT_METHODS.CREATED_DATE,
                                       FORTE_PAYMENT_METHODS.UPDATED_DATE,
                                       FORTE_PAYMENT_METHODS.KB_TENANT_ID)
                           .values(kbAccountId.toString(),
                                   kbPaymentMethodId.toString(),
                                   getProperty(FortePaymentPluginApi.PROPERTY_TOKEN, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_FIRST_NAME, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_LAST_NAME, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_TYPE, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_EXPIRATION_MONTH, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_EXPIRATION_YEAR, properties),
                                   ccNumber,
                                   ccLast4,
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_START_MONTH, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_START_YEAR, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_ISSUE_NUMBER, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_VERIFICATION_VALUE, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CC_TRACK_DATA, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_TRANSIT_ROUTING_NUMBER, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_ACCOUNT_NUMBER, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_ACCOUNT_TYPE, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_ADDRESS1, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_ADDRESS2, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_CITY, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_STATE, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_ZIP, properties),
                                   getProperty(FortePaymentPluginApi.PROPERTY_COUNTRY, properties),
                                   fromBoolean(isDefault),
                                   FALSE,
                                   getAdditionalData(properties),
                                   toTimestamp(utcNow),
                                   toTimestamp(utcNow),
                                   kbTenantId.toString())
                           .execute();
                        return null;
                    }
                });
    }

    public static List<PluginProperty> buildPluginProperties(@Nullable final String additionalData) {
        if (additionalData == null) {
            return ImmutableList.<PluginProperty>of();
        }

        final Map additionalDataMap;
        try {
            additionalDataMap = objectMapper.readValue(additionalData, Map.class);
        } catch (final IOException e) {
            return ImmutableList.<PluginProperty>of();
        }

        return PluginProperties.buildPluginProperties(additionalDataMap);
    }

    private String getAdditionalData(final Map additionalData) throws SQLException {
        if (additionalData == null || additionalData.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(additionalData);
        } catch (final JsonProcessingException e) {
            throw new SQLException(e);
        }
    }
}

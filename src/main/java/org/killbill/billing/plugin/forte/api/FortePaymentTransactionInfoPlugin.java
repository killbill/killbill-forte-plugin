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

package org.killbill.billing.plugin.forte.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.forte.client.ForteAGIClient;
import org.killbill.billing.plugin.forte.dao.ForteDao;
import org.killbill.billing.plugin.forte.dao.gen.tables.records.ForteResponsesRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FortePaymentTransactionInfoPlugin extends PluginPaymentTransactionInfoPlugin {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String RESPONSE_TYPE_APPROVAL = "A";

    public FortePaymentTransactionInfoPlugin(final ForteResponsesRecord record) {
        super(UUID.fromString(record.getKbPaymentId()),
              UUID.fromString(record.getKbPaymentTransactionId()),
              TransactionType.valueOf(record.getTransactionType()),
              record.getAmount(),
              record.getCurrency() == null ? null : Currency.valueOf(record.getCurrency()),
              getPluginStatus(record.getPgResponseType()),
              record.getPgResponseDescription(),
              record.getPgResponseCode(),
              record.getPgTraceNumber(),
              record.getPgAuthorizationCode(),
              new DateTime(record.getCreatedDate(), DateTimeZone.UTC),
              new DateTime(record.getCreatedDate(), DateTimeZone.UTC),
              ForteDao.buildPluginProperties(record.getAdditionalData()));
    }

    public FortePaymentTransactionInfoPlugin(final UUID kbPaymentId,
                                             final UUID kbTransactionId,
                                             final TransactionType transactionType,
                                             final BigDecimal amount,
                                             final Currency currency,
                                             final DateTime utcNow,
                                             final Map<String, String> response) {
        super(kbPaymentId,
              kbTransactionId,
              transactionType,
              amount,
              currency,
              getPluginStatus(response.get(ForteAGIClient.PG_RESPONSE_TYPE)),
              response.get(response.get(ForteAGIClient.PG_RESPONSE_DESCRIPTION)),
              response.get(response.get(ForteAGIClient.PG_RESPONSE_CODE)),
              response.get(response.get(ForteAGIClient.PG_TRACE_NUMBER)),
              response.get(response.get(ForteAGIClient.PG_AUTHORIZATION_CODE)),
              utcNow,
              utcNow,
              PluginProperties.buildPluginProperties(response));
    }

    private static PaymentPluginStatus getPluginStatus(final String responseType) {
        return RESPONSE_TYPE_APPROVAL.equals(responseType) ? PaymentPluginStatus.PROCESSED : PaymentPluginStatus.ERROR;
    }
}

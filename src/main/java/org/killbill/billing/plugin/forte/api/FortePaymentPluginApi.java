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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.GatewayNotification;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentPluginApi;
import org.killbill.billing.plugin.forte.client.ForteAGIClient;
import org.killbill.billing.plugin.forte.dao.ForteDao;
import org.killbill.billing.plugin.forte.dao.gen.tables.FortePaymentMethods;
import org.killbill.billing.plugin.forte.dao.gen.tables.ForteResponses;
import org.killbill.billing.plugin.forte.dao.gen.tables.records.FortePaymentMethodsRecord;
import org.killbill.billing.plugin.forte.dao.gen.tables.records.ForteResponsesRecord;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class FortePaymentPluginApi extends PluginPaymentPluginApi<ForteResponsesRecord, ForteResponses, FortePaymentMethodsRecord, FortePaymentMethods> {

    public static final String PROPERTY_FIRST_NAME = "firstName";
    public static final String PROPERTY_LAST_NAME = "lastName";
    public static final String PROPERTY_TRANSIT_ROUTING_NUMBER = "trn";
    public static final String PROPERTY_ACCOUNT_NUMBER = "accountNumber";
    public static final String PROPERTY_ACCOUNT_TYPE = "accountType";

    private static final String SOFTWARE_NAME = "KILLBILL";
    private static final String SOFTWARE_VERSION = "1.0";

    private final ForteAGIClient client;

    public FortePaymentPluginApi(final OSGIKillbillAPI killbillAPI, final OSGIConfigPropertiesService configProperties, final OSGIKillbillLogService logService, final Clock clock, final ForteDao dao) {
        super(killbillAPI, configProperties, logService, clock, dao);
        this.client = new ForteAGIClient(configProperties.getProperties());
    }

    @Override
    protected PaymentTransactionInfoPlugin buildPaymentTransactionInfoPlugin(final ForteResponsesRecord forteResponsesRecord) {
        return new FortePaymentTransactionInfoPlugin(forteResponsesRecord);
    }

    @Override
    protected PaymentMethodPlugin buildPaymentMethodPlugin(final FortePaymentMethodsRecord fortePaymentMethodsRecord) {
        return new FortePaymentMethodPlugin(fortePaymentMethodsRecord);
    }

    @Override
    protected PaymentMethodInfoPlugin buildPaymentMethodInfoPlugin(final FortePaymentMethodsRecord fortePaymentMethodsRecord) {
        return new FortePaymentMethodInfoPlugin(fortePaymentMethodsRecord);
    }

    @Override
    protected String getPaymentMethodId(final FortePaymentMethodsRecord fortePaymentMethodsRecord) {
        return fortePaymentMethodsRecord.getKbPaymentMethodId();
    }

    @Override
    public PaymentTransactionInfoPlugin authorizePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeTransaction(TransactionType.AUTHORIZE,
                                  new TransactionExecutor() {
                                      @Override
                                      public Map<String, String> execute(final BigDecimal amount, final String customerFirstName, final String customerLastName, final String cardName, final String cardType, final String cardNumber, final String cardExpMonth, final String cardExpYear, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createAuthTransaction(amount,
                                                                              customerFirstName,
                                                                              customerFirstName,
                                                                              cardName,
                                                                              cardType,
                                                                              cardNumber,
                                                                              cardExpMonth,
                                                                              cardExpYear,
                                                                              optionalData);
                                      }

                                      @Override
                                      public Map<String, String> execute(final BigDecimal amount, final String customerFirstName, final String customerLastName, final String transitRoutingNumber, final String accountNumber, final String accountType, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createAuthTransaction(amount,
                                                                              customerFirstName,
                                                                              customerFirstName,
                                                                              transitRoutingNumber,
                                                                              accountNumber,
                                                                              accountType,
                                                                              optionalData);
                                      }
                                  },
                                  kbAccountId,
                                  kbPaymentId,
                                  kbTransactionId,
                                  kbPaymentMethodId,
                                  amount,
                                  currency,
                                  properties,
                                  context);
    }

    @Override
    public PaymentTransactionInfoPlugin capturePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeTransaction(TransactionType.CAPTURE,
                                  new TransactionExecutor() {
                                      @Override
                                      public Map<String, String> execute(final String originalTraceNumber, final String originalAuthorizationCode, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createCaptureTransaction(originalTraceNumber,
                                                                                 originalAuthorizationCode,
                                                                                 optionalData);
                                      }
                                  },
                                  kbAccountId,
                                  kbPaymentId,
                                  kbTransactionId,
                                  kbPaymentMethodId,
                                  amount,
                                  currency,
                                  properties,
                                  context);
    }

    @Override
    public PaymentTransactionInfoPlugin purchasePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeTransaction(TransactionType.PURCHASE,
                                  new TransactionExecutor() {
                                      @Override
                                      public Map<String, String> execute(final BigDecimal amount, final String customerFirstName, final String customerLastName, final String cardName, final String cardType, final String cardNumber, final String cardExpMonth, final String cardExpYear, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createSaleTransaction(amount,
                                                                              customerFirstName,
                                                                              customerFirstName,
                                                                              cardName,
                                                                              cardType,
                                                                              cardNumber,
                                                                              cardExpMonth,
                                                                              cardExpYear,
                                                                              optionalData);
                                      }

                                      @Override
                                      public Map<String, String> execute(final BigDecimal amount, final String customerFirstName, final String customerLastName, final String transitRoutingNumber, final String accountNumber, final String accountType, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createSaleTransaction(amount,
                                                                              customerFirstName,
                                                                              customerFirstName,
                                                                              transitRoutingNumber,
                                                                              accountNumber,
                                                                              accountType,
                                                                              optionalData);
                                      }
                                  },
                                  kbAccountId,
                                  kbPaymentId,
                                  kbTransactionId,
                                  kbPaymentMethodId,
                                  amount,
                                  currency,
                                  properties,
                                  context);
    }

    @Override
    public PaymentTransactionInfoPlugin voidPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeTransaction(TransactionType.VOID,
                                  new TransactionExecutor() {
                                      @Override
                                      public Map<String, String> execute(final String originalTraceNumber, final String originalAuthorizationCode, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createVoidTransaction(originalTraceNumber,
                                                                              originalAuthorizationCode,
                                                                              optionalData);
                                      }
                                  },
                                  kbAccountId,
                                  kbPaymentId,
                                  kbTransactionId,
                                  kbPaymentMethodId,
                                  null,
                                  null,
                                  properties,
                                  context);
    }

    @Override
    public PaymentTransactionInfoPlugin creditPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeTransaction(TransactionType.CREDIT,
                                  new TransactionExecutor() {
                                      @Override
                                      public Map<String, String> execute(final BigDecimal amount, final String customerFirstName, final String customerLastName, final String cardName, final String cardType, final String cardNumber, final String cardExpMonth, final String cardExpYear, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createCreditTransaction(amount,
                                                                                customerFirstName,
                                                                                customerFirstName,
                                                                                cardName,
                                                                                cardType,
                                                                                cardNumber,
                                                                                cardExpMonth,
                                                                                cardExpYear,
                                                                                optionalData);
                                      }

                                      @Override
                                      public Map<String, String> execute(final BigDecimal amount, final String customerFirstName, final String customerLastName, final String transitRoutingNumber, final String accountNumber, final String accountType, @Nullable final Map<String, Object> optionalData) throws IOException {
                                          return client.createCreditTransaction(amount,
                                                                                customerFirstName,
                                                                                customerFirstName,
                                                                                transitRoutingNumber,
                                                                                accountNumber,
                                                                                accountType,
                                                                                optionalData);
                                      }
                                  },
                                  kbAccountId,
                                  kbPaymentId,
                                  kbTransactionId,
                                  kbPaymentMethodId,
                                  amount,
                                  currency,
                                  properties,
                                  context);
    }

    @Override
    public PaymentTransactionInfoPlugin refundPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        throw new PaymentPluginApiException(null, "REFUND: unsupported operation, use CREDIT instead");
    }

    // HPP

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID kbAccountId, final Iterable<PluginProperty> customFields, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        throw new PaymentPluginApiException(null, "BUILD FORM_DESCRIPTOR: unsupported operation");
    }

    @Override
    public GatewayNotification processNotification(final String notification, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        throw new PaymentPluginApiException(null, "PROCESS NOTIFICATION: unsupported operation");
    }

    private PaymentTransactionInfoPlugin executeTransaction(final TransactionType transactionType,
                                                            final TransactionExecutor transactionExecutor,
                                                            final UUID kbAccountId,
                                                            final UUID kbPaymentId,
                                                            final UUID kbTransactionId,
                                                            final UUID kbPaymentMethodId,
                                                            @Nullable final BigDecimal amount,
                                                            @Nullable final Currency currency,
                                                            final Iterable<PluginProperty> properties,
                                                            final CallContext context) throws PaymentPluginApiException {
        final Account account = getAccount(kbAccountId, context);
        final FortePaymentMethodsRecord paymentMethodsRecord = getPaymentMethodRecord(kbPaymentMethodId, context);
        final Map<String, Object> additionalData = buildAdditionalData(kbAccountId, kbPaymentId, kbTransactionId, kbPaymentMethodId, context);
        final DateTime utcNow = clock.getUTCNow();

        final String accountFirstName = account.getFirstNameLength() == null ? null : account.getName().substring(0, account.getFirstNameLength());
        final String customerFirstName = PluginProperties.getValue(PROPERTY_FIRST_NAME, accountFirstName, properties);

        final String accountLastName = account.getFirstNameLength() == null ? account.getName() : account.getName().substring(account.getFirstNameLength(), account.getName().length());
        final String customerLastName = PluginProperties.getValue(PROPERTY_LAST_NAME, accountLastName, properties);

        final String paymentMethodCcNumber = paymentMethodsRecord == null ? null : paymentMethodsRecord.getCcNumber();
        final String ccNumber = PluginProperties.getValue(PROPERTY_CC_NUMBER, paymentMethodCcNumber, properties);

        final String paymentMethodAccountNumber = paymentMethodsRecord == null ? null : paymentMethodsRecord.getAccountNumber();
        final String accountNumber = PluginProperties.getValue(PROPERTY_ACCOUNT_NUMBER, paymentMethodAccountNumber, properties);

        final Map<String, String> response;
        if (ccNumber != null) {
            // By convention, support the same keys as the Ruby plugins (https://github.com/killbill/killbill-plugin-framework-ruby/blob/master/lib/killbill/helpers/active_merchant/payment_plugin.rb)
            final String paymentMethodExpirationMonth = paymentMethodsRecord == null ? null : paymentMethodsRecord.getCcExpMonth();
            final String ccExpirationMonth = PluginProperties.getValue(PROPERTY_CC_EXPIRATION_MONTH, paymentMethodExpirationMonth, properties);

            final String paymentMethodExpirationYear = paymentMethodsRecord == null ? null : paymentMethodsRecord.getCcExpYear();
            final String ccExpirationYear = PluginProperties.getValue(PROPERTY_CC_EXPIRATION_YEAR, paymentMethodExpirationYear, properties);

            final String paymentMethodCcType = paymentMethodsRecord == null ? null : paymentMethodsRecord.getCcType();
            final String ccType = PluginProperties.getValue(PROPERTY_CC_TYPE, paymentMethodCcType, properties);

            final String paymentMethodCcFirstName = paymentMethodsRecord == null ? null : paymentMethodsRecord.getCcFirstName();
            final String ccFirstName = PluginProperties.getValue(PROPERTY_CC_FIRST_NAME, paymentMethodCcFirstName, properties);

            final String paymentMethodCcLastName = paymentMethodsRecord == null ? null : paymentMethodsRecord.getCcLastName();
            final String ccLastName = PluginProperties.getValue(PROPERTY_CC_LAST_NAME, paymentMethodCcLastName, properties);

            try {
                response = transactionExecutor.execute(amount,
                                                       customerFirstName,
                                                       customerLastName,
                                                       String.format("%s %s", ccFirstName, ccLastName),
                                                       ccType,
                                                       ccNumber,
                                                       ccExpirationMonth,
                                                       ccExpirationYear,
                                                       additionalData);
            } catch (IOException e) {
                throw new PaymentPluginApiException(null, e);
            }
        } else if (accountNumber != null) {
            // EFT transaction
            final String paymentMethodRoutingNumber = paymentMethodsRecord == null ? null : paymentMethodsRecord.getTransitRoutingNumber();
            final String transitRoutingNumber = PluginProperties.getValue(PROPERTY_TRANSIT_ROUTING_NUMBER, paymentMethodRoutingNumber, properties);

            final String paymentMethodAccountType = paymentMethodsRecord == null ? null : paymentMethodsRecord.getAccountType();
            final String accountType = PluginProperties.getValue(PROPERTY_ACCOUNT_TYPE, paymentMethodAccountType, properties);

            try {
                response = transactionExecutor.execute(amount,
                                                       customerFirstName,
                                                       customerLastName,
                                                       transitRoutingNumber,
                                                       accountNumber,
                                                       accountType,
                                                       additionalData);
            } catch (IOException e) {
                throw new PaymentPluginApiException(null, e);
            }
        } else {
            // Modification (capture, void)
            final String originalTraceNumber;
            final String originalAuthorizationCode;
            try {
                final ForteResponsesRecord previousResponse = dao.getSuccessfulAuthorizationResponse(kbPaymentId, context.getTenantId());
                if (previousResponse == null) {
                    throw new PaymentPluginApiException(null, "Unable to retrieve previous payment response for kbTransactionId " + kbTransactionId);
                }
                originalTraceNumber = previousResponse.getPgTraceNumber();
                originalAuthorizationCode = previousResponse.getPgAuthorizationCode();
            } catch (final SQLException e) {
                throw new PaymentPluginApiException("Unable to retrieve previous payment response for kbTransactionId " + kbTransactionId, e);
            }

            try {
                response = transactionExecutor.execute(originalTraceNumber,
                                                       originalAuthorizationCode,
                                                       additionalData);
            } catch (IOException e) {
                throw new PaymentPluginApiException(null, e);
            }
        }

        try {
            dao.addResponse(kbAccountId, kbPaymentId, kbTransactionId, transactionType, amount, currency, response, utcNow, context.getTenantId());
            return new FortePaymentTransactionInfoPlugin(kbPaymentId, kbTransactionId, transactionType, amount, currency, utcNow, response);
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Payment went through, but we encountered a database error. Payment details: " + response.toString(), e);
        }
    }

    private Map<String, Object> buildAdditionalData(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final CallContext context) {
        final Builder<String, Object> additionalDataBuilder = ImmutableMap.<String, Object>builder();
        additionalDataBuilder.put(ForteAGIClient.PG_CONSUMER_ID, kbAccountId);
        additionalDataBuilder.put(ForteAGIClient.ECOM_CONSUMERORDERID, kbPaymentId);
        additionalDataBuilder.put(ForteAGIClient.ECOM_WALLETID, kbPaymentMethodId);
        additionalDataBuilder.put(ForteAGIClient.PG_ENTERED_BY, Objects.firstNonNull(context.getUserName(), FortePaymentPluginApi.class));
        additionalDataBuilder.put(ForteAGIClient.PG_SOFTWARE_NAME, SOFTWARE_NAME);
        additionalDataBuilder.put(ForteAGIClient.PG_SOFTWARE_VERSION, SOFTWARE_VERSION);

        return additionalDataBuilder.build();
    }

    private FortePaymentMethodsRecord getPaymentMethodRecord(final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
        final FortePaymentMethodsRecord paymentMethodsRecord;
        try {
            paymentMethodsRecord = dao.getPaymentMethod(kbPaymentMethodId, context.getTenantId());
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to retrieve payment method for kbPaymentMethodId " + kbPaymentMethodId, e);
        }
        return paymentMethodsRecord;
    }

    private static abstract class TransactionExecutor {

        public Map<String, String> execute(final BigDecimal amount,
                                           final String customerFirstName,
                                           final String customerLastName,
                                           final String cardName,
                                           final String cardType,
                                           final String cardNumber,
                                           final String cardExpMonth,
                                           final String cardExpYear,
                                           @Nullable final Map<String, Object> optionalData) throws IOException {
            throw new UnsupportedOperationException();
        }

        public Map<String, String> execute(final BigDecimal amount,
                                           final String customerFirstName,
                                           final String customerLastName,
                                           final String transitRoutingNumber,
                                           final String accountNumber,
                                           final String accountType,
                                           @Nullable final Map<String, Object> optionalData) throws IOException {
            throw new UnsupportedOperationException();
        }

        public Map<String, String> execute(final String originalTraceNumber,
                                           final String originalAuthorizationCode,
                                           @Nullable final Map<String, Object> optionalData) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}

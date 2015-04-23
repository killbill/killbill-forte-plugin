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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

// See https://www.forte.net/devdocs/pdf/agi_integration.pdf
public class ForteAGIClient {

    public static final String PG_MERCHANT_ID = "pg_merchant_id";
    public static final String PG_PASSWORD = "pg_password";
    public static final String PG_TRANSACTION_TYPE = "pg_transaction_type";
    public static final String PG_MERCHANT_DATA_1 = "pg_merchant_data_1";
    public static final String PG_MERCHANT_DATA_2 = "pg_merchant_data_2";
    public static final String PG_MERCHANT_DATA_3 = "pg_merchant_data_3";
    public static final String PG_MERCHANT_DATA_4 = "pg_merchant_data_4";
    public static final String PG_MERCHANT_DATA_5 = "pg_merchant_data_5";
    public static final String PG_MERCHANT_DATA_6 = "pg_merchant_data_6";
    public static final String PG_MERCHANT_DATA_7 = "pg_merchant_data_7";
    public static final String PG_MERCHANT_DATA_8 = "pg_merchant_data_8";
    public static final String PG_MERCHANT_DATA_9 = "pg_merchant_data_9";
    public static final String PG_TOTAL_AMOUNT = "pg_total_amount";
    public static final String PG_SALES_TAX_AMOUNT = "pg_sales_tax_amount";
    public static final String PG_CONSUMER_ID = "pg_consumer_id";
    public static final String ECOM_CONSUMERORDERID = "ecom_consumerorderid";
    public static final String ECOM_WALLETID = "ecom_walletid";
    public static final String PG_CUSTOMER_TOKEN = "pg_customer_token";
    public static final String PG_CLIENT_ID = "pg_client_id";
    public static final String PG_BILLTO_POSTAL_NAME_COMPANY = "pg_billto_postal_name_company";
    public static final String ECOM_BILLTO_POSTAL_NAME_FIRST = "ecom_billto_postal_name_first";
    public static final String ECOM_BILLTO_POSTAL_NAME_LAST = "ecom_billto_postal_name_last";
    public static final String ECOM_BILLTO_POSTAL_STREET_LINE1 = "ecom_billto_postal_street_line1";
    public static final String ECOM_BILLTO_POSTAL_STREET_LINE2 = "ecom_billto_postal_street_line2";
    public static final String ECOM_BILLTO_POSTAL_CITY = "ecom_billto_postal_city";
    public static final String ECOM_BILLTO_POSTAL_STATEPROV = "ecom_billto_postal_stateprov";
    public static final String ECOM_BILLTO_POSTAL_POSTALCODE = "ecom_billto_postal_postalcode";
    public static final String ECOM_BILLTO_POSTAL_COUNTRYCODE = "ecom_billto_postal_countrycode";
    public static final String ECOM_BILLTO_TELECOM_PHONE_NUMBER = "ecom_billto_telecom_phone_number";
    public static final String ECOM_BILLTO_ONLINE_EMAIL = "ecom_billto_online_email";
    public static final String PG_BILLTO_SSN = "pg_billto_ssn";
    public static final String PG_BILLTO_DL_NUMBER = "pg_billto_dl_number";
    public static final String PG_BILLTO_DL_STATE = "pg_billto_dl_state";
    public static final String PG_BILLTO_DATE_OF_BIRTH = "pg_billto_date_of_birth";
    public static final String PG_ENTERED_BY = "pg_entered_by";
    public static final String PG_SCHEDULE_QUANTITY = "pg_schedule_quantity";
    public static final String PG_SCHEDULE_FREQUENCY = "pg_schedule_frequency";
    public static final String PG_SCHEDULE_RECURRING_AMOUNT = "pg_schedule_recurring_amount";
    public static final String PG_SCHEDULE_START_DATE = "pg_schedule_start_date";
    public static final String PG_CUSTOMER_IP_ADDRESS = "pg_customer_ip_address";
    public static final String PG_MERCHANT_RECURRING = "pg_merchant_recurring";
    public static final String PG_SOFTWARE_NAME = "pg_software_name";
    public static final String PG_SOFTWARE_VERSION = "pg_software_version";
    public static final String PG_AVS_METHOD = "pg_avs_method";
    public static final String ECOM_PAYMENT_CARD_TYPE = "ecom_payment_card_type";
    public static final String ECOM_PAYMENT_CARD_NAME = "ecom_payment_card_name";
    public static final String ECOM_PAYMENT_CARD_NUMBER = "ecom_payment_card_number";
    public static final String ECOM_PAYMENT_CARD_EXPDATE_MONTH = "ecom_payment_card_expdate_month";
    public static final String ECOM_PAYMENT_CARD_EXPDATE_YEAR = "ecom_payment_card_expdate_year";
    public static final String ECOM_PAYMENT_CARD_VERIFICATION = "ecom_payment_card_verification";
    public static final String PG_PROCUREMENT_CARD = "pg_procurement_card";
    public static final String PG_CUSTOMER_ACCT_CODE = "pg_customer_acct_code";
    public static final String PG_CC_SWIPE_DATA = "pg_cc_swipe_data";
    public static final String PG_CC_ENC_SWIPE_DATA = "pg_cc_enc_swipe_data";
    public static final String PG_CC_ENC_DECRYPTOR = "pg_cc_enc_decryptor";
    public static final String ECOM_3D_SECURE_DATA = "ecom_3d_secure_data";
    public static final String ECOM_3D_SECURE_AUTHENTICATED = "ecom_3d_secure_authenticated";
    public static final String PG_PARTIAL_AUTH_ALLOWED_FLAG = "pg_partial_auth_allowed_flag";
    public static final String PG_MAIL_OR_PHONE_ORDER = "pg_mail_or_phone_order";
    public static final String PG_PAYMENT_TOKEN = "pg_payment_token";
    public static final String PG_PAYMENT_METHOD_ID = "pg_payment_method_id";
    public static final String PG_ONETIME_TOKEN = "pg_onetime_token";
    public static final String ECOM_PAYMENT_CHECK_TRN = "ecom_payment_check_trn";
    public static final String ECOM_PAYMENT_CHECK_ACCOUNT = "ecom_payment_check_account";
    public static final String ECOM_PAYMENT_CHECK_ACCOUNT_TYPE = "ecom_payment_check_account_type";
    public static final String ECOM_PAYMENT_CHECK_CHECKNO = "ecom_payment_check_checkno";
    public static final String PG_ENTRY_CLASS_CODE = "pg_entry_class_code";
    public static final String PG_TRACE_NUMBER = "pg_trace_number";
    public static final String PG_AUTHORIZATION_CODE = "pg_authorization_code";
    public static final String PG_ORIGINAL_TRACE_NUMBER = "pg_original_trace_number";
    public static final String PG_ORIGINAL_AUTHORIZATION_CODE = "pg_original_authorization_code";
    public static final String PG_RESPONSE_TYPE = "pg_response_type";
    public static final String PG_RESPONSE_DESCRIPTION = "pg_response_description";
    public static final String PG_RESPONSE_CODE = "pg_response_code";
    public static final String PG_AVS_RESULT = "pg_avs_result";
    public static final String PG_PREAUTH_RESULT = "pg_preauth_result";
    public static final String PG_PREAUTH_DESCRIPTION = "pg_preauth_description";
    public static final String PG_PREAUTH_NEG_REPORT = "pg_preauth_neg_report";
    public static final String PG_CVV2_RESULT = "pg_cvv2_result";
    public static final String PG_3D_SECURE_RESULT = "pg_3d_secure_result";
    public static final String PG_AVAILABLE_CARD_BALANCE = "pg_available_card_balance";
    public static final String PG_REQUESTED_AMOUNT = "pg_requested_amount";
    public static final String PG_CONVENIENCE_FEE = "pg_convenience_fee";

    public static final String VISA = "VISA";
    public static final String MASTERCARD = "MAST";
    public static final String AMEX = "AMER";
    public static final String DISCOVER = "DISC";
    public static final String DINER = "DINE";
    public static final String JCB = "JCB";

    public static final String CREDIT_CARD_SALE = "10";
    public static final String CREDIT_CARD_AUTH = "11";
    public static final String CREDIT_CARD_CAPTURE = "12";
    public static final String CREDIT_CARD_CREDIT = "13";
    public static final String CREDIT_CARD_VOID = "14";
    public static final String CREDIT_CARD_PRE_AUTH = "15";
    public static final String CREDIT_CARD_BALANCE = "18";
    public static final String EFT_SALE = "20";
    public static final String EFT_AUTH = "21";
    public static final String EFT_CAPTURE = "22";
    public static final String EFT_CREDIT = "23";
    public static final String EFT_VOID = "24";
    public static final String EFT_FORCE = "25";
    public static final String EFT_VERIFY = "26";
    public static final String RECURRING_SUSPEND = "40";
    public static final String RECURRING_ACTIVATE = "41";
    public static final String RECURRING_DELETE = "42";

    private static final String DATE_FORMAT = "DD/MM/YYYY";
    private static final String FALSE = "FALSE";
    private static final String TRUE = "TRUE";
    private static final String ENDOFDATA = "endofdata";

    private static final Joiner MSG_LINE_JOINER = Joiner.on("=");
    private static final Joiner MSG_LINES_JOINER = Joiner.on("\n");

    public static final String PROPERTY_BASE = "org.killbill.billing.plugin.forte";
    public static final String PROPERTY_MERCHANT_ID = PROPERTY_BASE + ".merchantId";
    private static final String PROPERTY_MERCHANT_PASSWORD = PROPERTY_BASE + ".password";
    private static final String PROPERTY_HOST = PROPERTY_BASE + ".host";
    private static final String PROPERTY_PORT = PROPERTY_BASE + ".port";

    private final String merchantId;
    private final String password;
    private final String host;
    private final int port;
    private final SocketFactory factory;

    public ForteAGIClient(final Properties properties) {
        this(properties.getProperty(PROPERTY_MERCHANT_ID),
             properties.getProperty(PROPERTY_MERCHANT_PASSWORD),
             properties.getProperty(PROPERTY_HOST),
             properties.getProperty(PROPERTY_PORT) == null ? 6050 : Integer.parseInt(properties.getProperty(PROPERTY_PORT)));
    }

    private ForteAGIClient(final String merchantId, final String password, final String host, final Integer port) {
        this.merchantId = merchantId;
        this.password = password;
        this.host = host;
        this.port = port;
        this.factory = SSLSocketFactory.getDefault();
    }

    // Credit card transactions

    public <T> Map<String, String> createAuthTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         final String cardName,
                                                         final String cardType,
                                                         final String cardNumber,
                                                         final String cardExpMonth,
                                                         final String cardExpYear,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createAuthTransaction(amount,
                                     customerFirstName,
                                     customerLastName,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     cardName,
                                     cardType,
                                     cardNumber,
                                     cardExpMonth,
                                     cardExpYear,
                                     optionalData);
    }

    public <T> Map<String, String> createAuthTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         @Nullable final String customerStreetLine1,
                                                         @Nullable final String customerState,
                                                         @Nullable final String zip,
                                                         @Nullable final String phone,
                                                         @Nullable final String email,
                                                         final String cardName,
                                                         final String cardType,
                                                         final String cardNumber,
                                                         final String cardExpMonth,
                                                         final String cardExpYear,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createCreditCardTransaction(CREDIT_CARD_AUTH,
                                           amount,
                                           customerFirstName,
                                           customerLastName,
                                           customerStreetLine1,
                                           customerState,
                                           zip,
                                           phone,
                                           email,
                                           cardName,
                                           cardType,
                                           cardNumber,
                                           cardExpMonth,
                                           cardExpYear,
                                           optionalData);
    }

    public <T> Map<String, String> createCreditTransaction(final BigDecimal amount,
                                                           final String customerFirstName,
                                                           final String customerLastName,
                                                           final String cardName,
                                                           final String cardType,
                                                           final String cardNumber,
                                                           final String cardExpMonth,
                                                           final String cardExpYear,
                                                           @Nullable final Map<String, T> optionalData) throws IOException {
        return createCreditTransaction(amount,
                                       customerFirstName,
                                       customerLastName,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       cardName,
                                       cardType,
                                       cardNumber,
                                       cardExpMonth,
                                       cardExpYear,
                                       optionalData);
    }

    public <T> Map<String, String> createCreditTransaction(final BigDecimal amount,
                                                           final String customerFirstName,
                                                           final String customerLastName,
                                                           @Nullable final String customerStreetLine1,
                                                           @Nullable final String customerState,
                                                           @Nullable final String zip,
                                                           @Nullable final String phone,
                                                           @Nullable final String email,
                                                           final String cardName,
                                                           final String cardType,
                                                           final String cardNumber,
                                                           final String cardExpMonth,
                                                           final String cardExpYear,
                                                           @Nullable final Map<String, T> optionalData) throws IOException {
        return createCreditCardTransaction(CREDIT_CARD_CREDIT,
                                           amount,
                                           customerFirstName,
                                           customerLastName,
                                           customerStreetLine1,
                                           customerState,
                                           zip,
                                           phone,
                                           email,
                                           cardName,
                                           cardType,
                                           cardNumber,
                                           cardExpMonth,
                                           cardExpYear,
                                           optionalData);
    }

    public <T> Map<String, String> createSaleTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         final String cardName,
                                                         final String cardType,
                                                         final String cardNumber,
                                                         final String cardExpMonth,
                                                         final String cardExpYear,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createSaleTransaction(amount,
                                     customerFirstName,
                                     customerLastName,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     cardName,
                                     cardType,
                                     cardNumber,
                                     cardExpMonth,
                                     cardExpYear,
                                     optionalData);
    }

    public <T> Map<String, String> createSaleTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         @Nullable final String customerStreetLine1,
                                                         @Nullable final String customerState,
                                                         @Nullable final String zip,
                                                         @Nullable final String phone,
                                                         @Nullable final String email,
                                                         final String cardName,
                                                         final String cardType,
                                                         final String cardNumber,
                                                         final String cardExpMonth,
                                                         final String cardExpYear,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createCreditCardTransaction(CREDIT_CARD_SALE,
                                           amount,
                                           customerFirstName,
                                           customerLastName,
                                           customerStreetLine1,
                                           customerState,
                                           zip,
                                           phone,
                                           email,
                                           cardName,
                                           cardType,
                                           cardNumber,
                                           cardExpMonth,
                                           cardExpYear,
                                           optionalData);
    }

    // EFT transactions

    public <T> Map<String, String> createAuthTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         final String transitRoutingNumber,
                                                         final String accountNumber,
                                                         final String accountType,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createAuthTransaction(amount,
                                     customerFirstName,
                                     customerLastName,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     transitRoutingNumber,
                                     accountNumber,
                                     accountType,
                                     optionalData);
    }

    public <T> Map<String, String> createAuthTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         @Nullable final String customerStreetLine1,
                                                         @Nullable final String customerState,
                                                         @Nullable final String zip,
                                                         @Nullable final String phone,
                                                         @Nullable final String email,
                                                         final String transitRoutingNumber,
                                                         final String accountNumber,
                                                         final String accountType,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createEFTTransaction(EFT_AUTH,
                                    amount,
                                    customerFirstName,
                                    customerLastName,
                                    customerStreetLine1,
                                    customerState,
                                    zip,
                                    phone,
                                    email,
                                    transitRoutingNumber,
                                    accountNumber,
                                    accountType,
                                    optionalData);
    }

    public <T> Map<String, String> createCreditTransaction(final BigDecimal amount,
                                                           final String customerFirstName,
                                                           final String customerLastName,
                                                           final String transitRoutingNumber,
                                                           final String accountNumber,
                                                           final String accountType,
                                                           @Nullable final Map<String, T> optionalData) throws IOException {
        return createCreditTransaction(amount,
                                       customerFirstName,
                                       customerLastName,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       transitRoutingNumber,
                                       accountNumber,
                                       accountType,
                                       optionalData);
    }

    public <T> Map<String, String> createCreditTransaction(final BigDecimal amount,
                                                           final String customerFirstName,
                                                           final String customerLastName,
                                                           @Nullable final String customerStreetLine1,
                                                           @Nullable final String customerState,
                                                           @Nullable final String zip,
                                                           @Nullable final String phone,
                                                           @Nullable final String email,
                                                           final String transitRoutingNumber,
                                                           final String accountNumber,
                                                           final String accountType,
                                                           @Nullable final Map<String, T> optionalData) throws IOException {
        return createEFTTransaction(EFT_CREDIT,
                                    amount,
                                    customerFirstName,
                                    customerLastName,
                                    customerStreetLine1,
                                    customerState,
                                    zip,
                                    phone,
                                    email,
                                    transitRoutingNumber,
                                    accountNumber,
                                    accountType,
                                    optionalData);
    }

    public <T> Map<String, String> createSaleTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         final String transitRoutingNumber,
                                                         final String accountNumber,
                                                         final String accountType,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createSaleTransaction(amount,
                                     customerFirstName,
                                     customerLastName,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     transitRoutingNumber,
                                     accountNumber,
                                     accountType,
                                     optionalData);
    }

    public <T> Map<String, String> createSaleTransaction(final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         @Nullable final String customerStreetLine1,
                                                         @Nullable final String customerState,
                                                         @Nullable final String zip,
                                                         @Nullable final String phone,
                                                         @Nullable final String email,
                                                         final String transitRoutingNumber,
                                                         final String accountNumber,
                                                         final String accountType,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createEFTTransaction(EFT_SALE,
                                    amount,
                                    customerFirstName,
                                    customerLastName,
                                    customerStreetLine1,
                                    customerState,
                                    zip,
                                    phone,
                                    email,
                                    transitRoutingNumber,
                                    accountNumber,
                                    accountType,
                                    optionalData);
    }

    // Both credit card and EFT transactions

    public <T> Map<String, String> createCaptureTransaction(final String originalTraceNumber,
                                                            final String originalAuthorizationCode,
                                                            @Nullable final Map<String, T> optionalData) throws IOException {
        return createAdministrativeTransaction(EFT_CAPTURE, originalTraceNumber, originalAuthorizationCode, optionalData);
    }

    public <T> Map<String, String> createVoidTransaction(final String originalTraceNumber,
                                                         final String originalAuthorizationCode,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        return createAdministrativeTransaction(EFT_VOID, originalTraceNumber, originalAuthorizationCode, optionalData);
    }

    private <T> Map<String, String> createCreditCardTransaction(final String transactionType,
                                                                final BigDecimal amount,
                                                                final String customerFirstName,
                                                                final String customerLastName,
                                                                @Nullable final String customerStreetLine1,
                                                                @Nullable final String customerState,
                                                                @Nullable final String zip,
                                                                @Nullable final String phone,
                                                                @Nullable final String email,
                                                                @Nullable final String cardName,
                                                                @Nullable final String cardType,
                                                                @Nullable final String cardNumber,
                                                                @Nullable final String cardExpMonth,
                                                                @Nullable final String cardExpYear,
                                                                @Nullable final Map<String, T> optionalData) throws IOException {
        final Builder<String, Object> additionalDataBuilder = ImmutableMap.<String, Object>builder();
        if (cardName != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CARD_NAME, cardName);
        }
        if (cardType != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CARD_TYPE, cardType);
        }
        if (cardNumber != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CARD_NUMBER, cardNumber);
        }
        if (cardExpMonth != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CARD_EXPDATE_MONTH, cardExpMonth);
        }
        if (cardExpYear != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CARD_EXPDATE_YEAR, cardExpYear);
        }

        return createTCreditCardOrEFTTransaction(transactionType,
                                                 amount,
                                                 customerFirstName,
                                                 customerLastName,
                                                 customerStreetLine1,
                                                 customerState,
                                                 zip,
                                                 phone,
                                                 email,
                                                 optionalData,
                                                 additionalDataBuilder);
    }

    private <T> Map<String, String> createEFTTransaction(final String transactionType,
                                                         final BigDecimal amount,
                                                         final String customerFirstName,
                                                         final String customerLastName,
                                                         @Nullable final String customerStreetLine1,
                                                         @Nullable final String customerState,
                                                         @Nullable final String zip,
                                                         @Nullable final String phone,
                                                         @Nullable final String email,
                                                         @Nullable final String transitRoutingNumber,
                                                         @Nullable final String accountNumber,
                                                         @Nullable final String accountType,
                                                         @Nullable final Map<String, T> optionalData) throws IOException {
        final Builder<String, Object> additionalDataBuilder = ImmutableMap.<String, Object>builder();
        if (transitRoutingNumber != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CHECK_TRN, transitRoutingNumber);
        }
        if (accountNumber != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CHECK_ACCOUNT, accountNumber);
        }
        if (accountType != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_PAYMENT_CHECK_ACCOUNT_TYPE, accountType);
        }

        return createTCreditCardOrEFTTransaction(transactionType,
                                                 amount,
                                                 customerFirstName,
                                                 customerLastName,
                                                 customerStreetLine1,
                                                 customerState,
                                                 zip,
                                                 phone,
                                                 email,
                                                 optionalData,
                                                 additionalDataBuilder);
    }

    private <T> Map<String, String> createTCreditCardOrEFTTransaction(final String transactionType,
                                                                      final BigDecimal amount,
                                                                      final String customerFirstName,
                                                                      final String customerLastName,
                                                                      @Nullable final String customerStreetLine1,
                                                                      @Nullable final String customerState,
                                                                      @Nullable final String zip,
                                                                      @Nullable final String phone,
                                                                      @Nullable final String email,
                                                                      @Nullable final Map<String, T> optionalData,
                                                                      final Builder<String, Object> additionalDataBuilder) throws IOException {
        additionalDataBuilder.put(PG_TOTAL_AMOUNT, toString(amount));
        additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_FIRST, customerFirstName);
        additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_POSTAL_NAME_LAST, customerLastName);
        if (customerStreetLine1 != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_POSTAL_STREET_LINE1, customerStreetLine1);
        }
        if (customerState != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_POSTAL_STATEPROV, customerState);
        }
        if (zip != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_POSTAL_POSTALCODE, zip);
        }
        if (phone != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_TELECOM_PHONE_NUMBER, phone);
        }
        if (email != null) {
            additionalDataBuilder.put(ForteAGIClient.ECOM_BILLTO_ONLINE_EMAIL, email);
        }
        if (optionalData != null) {
            additionalDataBuilder.putAll(optionalData);
        }
        final ImmutableMap<String, Object> additionalData = additionalDataBuilder.build();

        return createTransaction(transactionType, additionalData);
    }

    private <T> Map<String, String> createAdministrativeTransaction(final String transactionType,
                                                                    final String originalTraceNumber,
                                                                    final String originalAuthorizationCode,
                                                                    @Nullable final Map<String, T> optionalData) throws IOException {
        final Builder<String, Object> additionalDataBuilder = ImmutableMap.<String, Object>builder();
        additionalDataBuilder.put(ForteAGIClient.PG_ORIGINAL_TRACE_NUMBER, originalTraceNumber);
        additionalDataBuilder.put(ForteAGIClient.PG_ORIGINAL_AUTHORIZATION_CODE, originalAuthorizationCode);
        if (optionalData != null) {
            additionalDataBuilder.putAll(optionalData);
        }
        final ImmutableMap<String, Object> additionalData = additionalDataBuilder.build();

        return createTransaction(transactionType, additionalData);
    }

    private Map<String, String> createTransaction(final String transactionType, final ImmutableMap<String, Object> additionalData) throws IOException {
        final Map<String, String> request = buildRequest(transactionType, additionalData);
        final Map<String, String> response = new HashMap<String, String>();

        validateRequest(request);
        sendAndReceiveData(request, response);

        return response;
    }

    private <T> Map<String, String> buildRequest(final String transactionType, final Map<String, T> additionalData) {
        final Builder<String, String> builder = ImmutableMap.<String, String>builder();

        builder.put(PG_MERCHANT_ID, merchantId);
        builder.put(PG_PASSWORD, password);

        builder.put(PG_TRANSACTION_TYPE, transactionType);

        final Map<String, String> additionalDataAsStrings = Maps.<String, T, String>transformValues(additionalData, TO_STRING);
        builder.putAll(additionalDataAsStrings);

        return builder.build();
    }

    private void validateRequest(final Map<String, String> request) {
        Preconditions.checkNotNull(request.get(PG_MERCHANT_ID), PG_MERCHANT_ID + " must be specified");
        Preconditions.checkNotNull(request.get(PG_PASSWORD), PG_PASSWORD + " must be specified");
        Preconditions.checkNotNull(request.get(PG_TRANSACTION_TYPE), PG_TRANSACTION_TYPE + " must be specified");

        if (request.get(ECOM_PAYMENT_CARD_NUMBER) != null) {
            // Credit card transaction
            Preconditions.checkNotNull(request.get(PG_TOTAL_AMOUNT), PG_TOTAL_AMOUNT + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_BILLTO_POSTAL_NAME_FIRST), ECOM_BILLTO_POSTAL_NAME_FIRST + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_BILLTO_POSTAL_NAME_LAST), ECOM_BILLTO_POSTAL_NAME_LAST + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_PAYMENT_CARD_TYPE), ECOM_PAYMENT_CARD_TYPE + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_PAYMENT_CARD_NAME), ECOM_PAYMENT_CARD_NAME + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_PAYMENT_CARD_EXPDATE_MONTH), ECOM_PAYMENT_CARD_EXPDATE_MONTH + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_PAYMENT_CARD_EXPDATE_YEAR), ECOM_PAYMENT_CARD_EXPDATE_YEAR + " must be specified");
        } else if (request.get(ECOM_PAYMENT_CHECK_ACCOUNT) != null) {
            // EFT transaction
            Preconditions.checkNotNull(request.get(PG_TOTAL_AMOUNT), PG_TOTAL_AMOUNT + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_BILLTO_POSTAL_NAME_FIRST), ECOM_BILLTO_POSTAL_NAME_FIRST + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_BILLTO_POSTAL_NAME_LAST), ECOM_BILLTO_POSTAL_NAME_LAST + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_PAYMENT_CHECK_TRN), ECOM_PAYMENT_CHECK_TRN + " must be specified");
            Preconditions.checkNotNull(request.get(ECOM_PAYMENT_CHECK_ACCOUNT_TYPE), ECOM_PAYMENT_CHECK_ACCOUNT_TYPE + " must be specified");
        } else if (request.get(PG_PAYMENT_METHOD_ID) != null) {
            Preconditions.checkNotNull(request.get(PG_TOTAL_AMOUNT), PG_TOTAL_AMOUNT + " must be specified");
        } else {
            // Administrative message
            Preconditions.checkNotNull(request.get(PG_ORIGINAL_TRACE_NUMBER), PG_ORIGINAL_TRACE_NUMBER + " must be specified");
        }
    }

    private void sendAndReceiveData(final Map<String, String> request, final Map<String, String> response) throws IOException {
        SSLSocket socket = null;
        try {
            socket = (SSLSocket) factory.createSocket(host, port);
            socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
            sendAndReceiveData(request, response, socket);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private void sendAndReceiveData(final Map<String, String> request, final Map<String, String> response, final SSLSocket socket) throws IOException {
        final Iterable<String> msgLines = Iterables.<Entry<String, String>, String>transform(request.entrySet(),
                                                                                             new Function<Entry<String, String>, String>() {
                                                                                                 @Override
                                                                                                 public String apply(final Map.Entry<String, String> input) {
                                                                                                     return MSG_LINE_JOINER.join(input.getKey(), input.getValue());
                                                                                                 }
                                                                                             });
        final String content = MSG_LINES_JOINER.join(msgLines) + "\n" + ENDOFDATA + "\n";

        // Send data
        final DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes(content);
        dos.flush();

        // Read the response
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();
            while (line != null) {
                // Check for end of message
                if (line.equals(ENDOFDATA)) {
                    break;
                }

                final int equalPos = line.indexOf('=');
                final String name = line.substring(0, equalPos);
                final String value = line.substring(equalPos + 1);
                response.put(name, value);

                // Get next line
                line = br.readLine();
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    private static final Function<Object, String> TO_STRING = new Function<Object, String>() {
        public String apply(final Object in) {
            return ForteAGIClient.toString(in);
        }
    };

    private static String toString(@Nullable final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof BigDecimal) {
            return toString((BigDecimal) value);
        } else if (value instanceof DateTime) {
            return toString((DateTime) value);
        } else if (value instanceof Boolean) {
            return toString((Boolean) value);
        } else {
            return value.toString();
        }
    }

    private static String toString(final BigDecimal money) {
        return money.toString();
    }

    private static String toString(final DateTime dateTime) {
        return dateTime.toString(DATE_FORMAT);
    }

    private static String toString(final Boolean bool) {
        return bool == null || !bool ? FALSE : TRUE;
    }
}

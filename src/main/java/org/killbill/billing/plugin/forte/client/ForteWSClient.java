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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.logging.Slf4jLogger;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.killbill.billing.plugin.forte.client.ws.HttpHeaderInterceptor;
import org.killbill.billing.plugin.forte.client.ws.LoggingInInterceptor;
import org.killbill.billing.plugin.forte.client.ws.LoggingOutInterceptor;

import com.google.common.annotations.VisibleForTesting;
import https.ws_paymentsgateway_net.v1.Authentication;
import https.ws_paymentsgateway_net.v1.CcCardType;
import https.ws_paymentsgateway_net.v1.ClientRecord;
import https.ws_paymentsgateway_net.v1.ClientService;
import https.ws_paymentsgateway_net.v1.EcAccountType;
import https.ws_paymentsgateway_net.v1.IClientService;
import https.ws_paymentsgateway_net.v1.PaymentMethod;

import static org.killbill.billing.plugin.forte.client.ForteAGIClient.PROPERTY_BASE;
import static org.killbill.billing.plugin.forte.client.ForteAGIClient.PROPERTY_MERCHANT_ID;

// See http://www.paymentsgateway.com/developerDocumentation/Integration/webservices/merchantservice.aspx
public class ForteWSClient {

    private static final String PROPERTY_API_LOGIN_ID = PROPERTY_BASE + ".apiLoginId";
    private static final String PROPERTY_SECURE_TRANSACTION_KEY = PROPERTY_BASE + ".secureTransactionKey";
    private static final String PROPERTY_TEST = PROPERTY_BASE + ".test";
    private static final String HMAC_MD5 = "HmacMD5";

    private final int merchantId;
    private final String apiLoginId;
    private final SecretKeySpec key;
    private final IClientService client;

    public ForteWSClient(final Properties properties) {
        this(Integer.parseInt(properties.getProperty(PROPERTY_MERCHANT_ID)),
             properties.getProperty(PROPERTY_API_LOGIN_ID),
             properties.getProperty(PROPERTY_SECURE_TRANSACTION_KEY),
             properties.getProperty(PROPERTY_TEST) == null ? false : Boolean.valueOf(properties.getProperty(PROPERTY_TEST)));
    }

    public ForteWSClient(final int merchantId, final String apiLoginId, final String secureTransactionKey, final Boolean test) {
        this.merchantId = merchantId;
        this.apiLoginId = apiLoginId;
        try {
            this.key = new SecretKeySpec(secureTransactionKey.getBytes("UTF-8"), HMAC_MD5);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        final String url = test ? "https://sandbox.paymentsgateway.net/ws/Client.svc" : "https://ws.paymentsgateway.net/Service/v1/Client.svc";
        this.client = createClient(url);
    }

    public String createClient(final String customerFirstName,
                               final String customerLastName,
                               @Nullable final String customerStreetLine1,
                               @Nullable final String customerState,
                               @Nullable final String zip,
                               @Nullable final String phone,
                               @Nullable final String email) {
        final ClientRecord clientRecord = new ClientRecord();
        clientRecord.setMerchantID(merchantId);
        clientRecord.setFirstName(customerFirstName);
        clientRecord.setLastName(customerLastName);
        clientRecord.setAddress1(customerStreetLine1);
        clientRecord.setState(customerState);
        clientRecord.setPostalCode(zip);
        clientRecord.setPhoneNumber(phone);
        clientRecord.setEmailAddress(email);

        return String.valueOf(client.createClient(buildAuthentication(), clientRecord));
    }

    public String tokenizeCreditCard(final String cardName,
                                     final String cardType,
                                     final String cardNumber,
                                     final String cardExpMonth,
                                     final String cardExpYear) {
        final PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setMerchantID(merchantId);
        paymentMethod.setAcctHolderName(cardName);
        paymentMethod.setCcCardType(CcCardType.fromValue(cardType.toUpperCase()));
        paymentMethod.setCcCardNumber(cardNumber);
        paymentMethod.setCcExpirationDate(cardExpYear + cardExpMonth);

        return String.valueOf(client.createPaymentMethod(buildAuthentication(), paymentMethod));
    }

    public String tokenizeECheck(final String accountHolderName,
                                 final String transitRoutingNumber,
                                 final String accountNumber,
                                 final String accountType) {
        final PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setMerchantID(merchantId);
        paymentMethod.setAcctHolderName(accountHolderName);
        paymentMethod.setEcAccountTRN(transitRoutingNumber);
        paymentMethod.setEcAccountNumber(accountNumber);
        paymentMethod.setEcAccountType("C".equalsIgnoreCase(accountType) || EcAccountType.CHECKING.value().equalsIgnoreCase(accountType) ? EcAccountType.CHECKING : EcAccountType.SAVINGS);

        return String.valueOf(client.createPaymentMethod(buildAuthentication(), paymentMethod));
    }

    private IClientService createClient(final String url) {
        // Delegate logging to slf4j (see also https://github.com/killbill/killbill-platform/tree/master/osgi-bundles/libs/slf4j-osgi)
        LogUtils.setLoggerClass(Slf4jLogger.class);

        final ClientService clientService = new ClientService();
        final IClientService client = clientService.getWSHttpBindingIClientService(new AddressingFeature());

        final BindingProvider bindingProvider = (BindingProvider) client;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        final Client clientProxy = ClientProxy.getClient(client);
        clientProxy.getInInterceptors().add(new LoggingInInterceptor());
        clientProxy.getOutInterceptors().add(new LoggingOutInterceptor());
        clientProxy.getOutInterceptors().add(new HttpHeaderInterceptor());
        //clientProxy.getRequestContext().put("org.apache.cxf.http.no_io_exceptions", "true");

        return client;
    }

    private Authentication buildAuthentication() {
        final String utcInTicks = generateUTCInTicks(System.currentTimeMillis());
        final String tSHash = generateTSHash(utcInTicks);

        final Authentication authentication = new Authentication();
        authentication.setAPILoginID(apiLoginId);
        authentication.setTSHash(tSHash);
        authentication.setUTCTime(utcInTicks);

        return authentication;
    }

    @VisibleForTesting
    String generateUTCInTicks(final Long utcTimeMillis) {
        return String.valueOf(utcTimeMillis / 1000 + 62135596800L) + "0000000";
    }

    @VisibleForTesting
    String generateTSHash(final String utcInTicks) {
        final String msg = apiLoginId + "|" + utcInTicks;

        String digest = null;
        try {
            Mac mac = Mac.getInstance(HMAC_MD5);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

            StringBuilder hash = new StringBuilder();
            for (final byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            digest = hash.toString();
        } catch (UnsupportedEncodingException e) {
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }

        return digest;
    }
}

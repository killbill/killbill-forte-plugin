killbill-forte-plugin
======================

Plugin to use [Forte](http://www.forte.net/) as a gateway.

Release builds are available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kill-bill.billing.plugin.java%22%20AND%20a%3A%22forte-plugin%22) with coordinates `org.kill-bill.billing.plugin.java:forte-plugin`.

Kill Bill compatibility
-----------------------

| Plugin version | Kill Bill version |
| -------------: | ----------------: |
| 0.x.y          | 0.14.z            |

Requirements
------------

The plugin needs a database. The latest version of the schema can be found [here](https://github.com/killbill/killbill-forte-plugin/blob/master/src/main/resources/ddl.sql).

Configuration
-------------

The following System Properties are required:

* `org.killbill.billing.plugin.forte.merchantId`: your merchant id
* `org.killbill.billing.plugin.forte.password`: your password
* `org.killbill.billing.plugin.forte.host`: AGI host (e.g. www.paymentsgateway.net)
* `org.killbill.billing.plugin.forte.port`: AGI port (e.g. 6050)
* `org.killbill.billing.plugin.forte.apiLoginId`: your API login id
* `org.killbill.billing.plugin.forte.secureTransactionKey`: your transaction key
* `org.killbill.billing.plugin.forte.test`: _true_ to use the sandbox

Usage
-----

Add a payment method (Bank Of America checking account):

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey: bob" \
     -H "X-Killbill-ApiSecret: lazar" \
     -H "Content-Type: application/json" \
     -H "X-Killbill-CreatedBy: demo" \
     -X POST \
     --data-binary '{
       "pluginName": "killbill-forte",
       "pluginInfo": {
         "properties": [
           {
             "key": "accountHolderName",
             "value": "Bob Smith"
           },
           {
             "key": "trn",
             "value": "122400724"
           },
           {
             "key": "accountNumber",
             "value": "123456789"
           },
           {
             "key": "accountType",
             "value": "C"
           }
         ]
       }
     }' \
     "http://127.0.0.1:8080/1.0/kb/accounts/<ACCOUNT_ID>/paymentMethods?isDefault=true"
```

Notes:
* Make sure to replace *ACCOUNT_ID* with the id of the Kill Bill account

To trigger a payment:

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey: bob" \
     -H "X-Killbill-ApiSecret: lazar" \
     -H "Content-Type: application/json" \
     -H "X-Killbill-CreatedBy: demo" \
     -X POST \
     --data-binary '{"transactionType":"PURCHASE","amount":"500","currency":"USD","transactionExternalKey":"INV-'$(uuidgen)'-PURCHASE"}' \
    "http://127.0.0.1:8080/1.0/kb/accounts/<ACCOUNT_ID>/payments"
```

Notes:
* Make sure to replace *ACCOUNT_ID* with the id of the Kill Bill account

You can verify the payment via:

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey: bob" \
     -H "X-Killbill-ApiSecret: lazar" \
     "http://127.0.0.1:8080/1.0/kb/accounts/<ACCOUNT_ID>/payments?withPluginInfo=true"
```

Notes:
* Make sure to replace *ACCOUNT_ID* with the id of the Kill Bill account

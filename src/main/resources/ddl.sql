/*! SET storage_engine=INNODB */;

drop table if exists forte_responses;
create table forte_responses (
  record_id int(11) unsigned not null auto_increment
, kb_account_id char(36) not null
, kb_payment_id char(36) not null
, kb_payment_transaction_id char(36) not null
, transaction_type varchar(32) not null
, amount numeric(15,9)
, currency char(3)
, pg_merchant_id varchar(256)
, pg_transaction_type varchar(256)
, pg_merchant_data_1 varchar(256)
, pg_merchant_data_2 varchar(256)
, pg_merchant_data_3 varchar(256)
, pg_merchant_data_4 varchar(256)
, pg_merchant_data_5 varchar(256)
, pg_merchant_data_6 varchar(256)
, pg_merchant_data_7 varchar(256)
, pg_merchant_data_8 varchar(256)
, pg_merchant_data_9 varchar(256)
, pg_total_amount varchar(256)
, pg_sales_tax_amount varchar(256)
, pg_customer_token varchar(256)
, pg_client_id varchar(256)
, pg_consumer_id varchar(256)
, ecom_consumerorderid varchar(256)
, pg_payment_token varchar(256)
, pg_payment_method_id varchar(256)
, ecom_walletid varchar(256)
, ecom_billto_postal_name_first varchar(256)
, ecom_billto_postal_name_last varchar(256)
, pg_billto_postal_name_company varchar(256)
, ecom_billto_online_email varchar(256)
, pg_response_type varchar(256)
, pg_response_code varchar(256)
, pg_response_description varchar(256)
, pg_avs_result varchar(256)
, pg_trace_number varchar(128)
, pg_authorization_code varchar(128)
, pg_preauth_result varchar(256)
, pg_preauth_description varchar(256)
, pg_preauth_neg_report varchar(256)
, pg_cvv2_result varchar(256)
, pg_3d_secure_result varchar(256)
, pg_available_card_balance varchar(256)
, pg_requested_amount varchar(256)
, pg_convenience_fee varchar(256)
, additional_data longtext
, created_date datetime not null
, kb_tenant_id char(36) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index forte_responses_pg_trace_number on forte_responses(pg_trace_number);
create index forte_responses_pg_authorization_code on forte_responses(pg_authorization_code);
create index forte_responses_kb_payment_id on forte_responses(kb_payment_id);
create index forte_responses_kb_payment_transaction_id on forte_responses(kb_payment_transaction_id);

drop table if exists forte_payment_methods;
create table forte_payment_methods (
  record_id int(11) unsigned not null auto_increment
, kb_account_id char(36) not null
, kb_payment_method_id char(36) not null
, token varchar(255) default null
, cc_first_name varchar(255) default null
, cc_last_name varchar(255) default null
, cc_type varchar(255) default null
, cc_exp_month varchar(255) default null
, cc_exp_year varchar(255) default null
, cc_number varchar(255) default null
, cc_last_4 varchar(255) default null
, cc_start_month varchar(255) default null
, cc_start_year varchar(255) default null
, cc_issue_number varchar(255) default null
, cc_verification_value varchar(255) default null
, cc_track_data varchar(255) default null
, transit_routing_number varchar(255) default null
, account_number varchar(255) default null
, account_type varchar(255) default null
, address1 varchar(255) default null
, address2 varchar(255) default null
, city varchar(255) default null
, state varchar(255) default null
, zip varchar(255) default null
, country varchar(255) default null
, is_default boolean not null default false
, is_deleted boolean not null default false
, additional_data longtext
, created_date datetime not null
, updated_date datetime not null
, kb_tenant_id char(36) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create unique index forte_payment_methods_kb_payment_id on forte_payment_methods(kb_payment_method_id);

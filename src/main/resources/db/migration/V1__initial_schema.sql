create table "account" (
"id" VARCHAR NOT NULL PRIMARY KEY,
"account_name" VARCHAR NOT NULL,
"account_number" DECIMAL(21,2) NOT NULL,
"number_code" VARCHAR NOT NULL,
"account_type" INTEGER NOT NULL,
"address" VARCHAR NOT NULL,
"bank_id" INTEGER NOT NULL,
"bank_id_code" VARCHAR NOT NULL,
"name" VARCHAR NOT NULL);


create table "payment" (
"id" VARCHAR NOT NULL PRIMARY KEY,
"amount" REAL NOT NULL,
"currency" VARCHAR NOT NULL,
"bearer_code" VARCHAR NOT NULL,
"sender_charges" VARCHAR NOT NULL,
"receiver_charges_amount" REAL NOT NULL,
"receiver_charges_currency" VARCHAR NOT NULL,
"end_to_end_reference" VARCHAR NOT NULL,
"numeric_reference" INTEGER NOT NULL,
"payment_id" DECIMAL(21,2) NOT NULL,
"payment_purpose" VARCHAR NOT NULL,
"payment_scheme" VARCHAR NOT NULL,
"payment_type" VARCHAR NOT NULL,
"processing_date" DATE NOT NULL,
"reference" VARCHAR NOT NULL,
"scheme_payment_subtype" VARCHAR NOT NULL,
"scheme_payment_type" VARCHAR NOT NULL,
"contract_reference" VARCHAR NOT NULL,
"exchange_rate" REAL NOT NULL,
"original_amount" REAL NOT NULL,
"original_currency" VARCHAR NOT NULL,
"beneficiary_id" VARCHAR NOT NULL,
"sponsor_id" VARCHAR NOT NULL,
"debtor_id" VARCHAR NOT NULL);

alter table "payment" add constraint "beneficiary_fk" foreign key("beneficiary_id") references "account"("id") on update SET NULL on delete SET NULL;
alter table "payment" add constraint "debtor_fk" foreign key("debtor_id") references "account"("id") on update SET NULL on delete SET NULL;
alter table "payment" add constraint "sponsor_fk" foreign key("sponsor_id") references "account"("id") on update SET NULL on delete SET NULL;

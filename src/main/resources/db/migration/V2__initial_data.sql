

insert into account ( id, account_name, account_number, number_code,
account_type, address, bank_id, bank_id_code, name)
values('1', 'beneficiary account name', 32432535, 'number code',
3434, ' address' , 633 , ' bank code ' , 'name');

insert into account ( id, account_name, account_number, number_code,
account_type, address, bank_id, bank_id_code, name)
values('2', 'debtor account name', 32432535, 'number code',
3434, ' address' , 633 , ' bank code ' , 'name');

insert into account ( id, account_name, account_number, number_code,
account_type, address, bank_id, bank_id_code, name)
values('3', 'sponsor account name', 32432535, 'number code',
3434, ' address' , 633 , ' bank code ' , 'name');


insert into payment ( id, amount, currency, bearer_code, receiver_charges_amount, receiver_charges_currency,
end_to_end_reference, numeric_reference, payment_id, payment_purpose, payment_scheme, payment_type, processing_date, sender_charges,
reference, scheme_payment_subtype, scheme_payment_type, contract_reference, exchange_rate, original_amount, original_currency,
beneficiary_id, debtor_id, sponsor_id)
values('1', 3.5, 'GBP', 'BST', 35.44, 'receiver_cur',
' e2e reference', 1234, 53252353263, 'payment purpose', 'payment scheme', ' pyament type ', '2018-01-02', '34.55 GBP;5 USD',
' reference', 'scheme payment subtype' , ' scheme type ' , ' contract reference' , 33.55, 66.77, 'USD',
'1', '2', '3');
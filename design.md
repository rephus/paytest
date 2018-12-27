I noticed the example given is using a format similar to Jsonapi, we would need a serializer to translate the database models into the Jsonapi format, but this is an  implementation detail.

Jsonapi encourages to add external entities as references in the Json output, however I noticed in this example that everything is inside attributes, so I'll keep this format to match the example on the implementation.

I considered version and organisation_id (maybe some kind of internal client id?) to be api versioning information, therefore I did not include them in the models.

Beneficiary, debtor and sponsor parties are all bank detail references, as they share msot of the attributes and can be reused across different payments.

To avoid payment conflicts, we could allow creation on bank_details and not updates. If a person changes is bank details, a new entity is create. Optionally, we could add a flag to mark outdated bank details.

Foreigh exchange is specific for every payment, and a such it doesn't make sense to store as a separate resource. 

I decided to store charges as an array of json to keep currency and amount separate. I considered this is not going to be queryeable otherwise I would store them as an auxiliary table. 

Internal ids are going to be stored as UUIDs in the database (as the one in the payment example)

There was no requirement for a bank_detail (beneficiary, debtor or sponsor external references) api, so I have just added a POST for testing purposes.

In principle, I consider all fields in Payment are mandatory.

I am not familiar with the meaning of some fields like the difference between reference or end_to_end_reference, or why there are multiple sender_charges in different currencies; so I decided to keep the model to respect the format.  
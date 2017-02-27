[dataset]
```property
URL = https://data.cityofchicago.org/api/views/uupf-x98q
Catalog URL = 
Id = uupf-x98q
Name = Business Licenses - Current Active
Attribution = City of Chicago
Attribution Link = http://www.cityofchicago.org
Category = Community & Economic Development
Tags = [business, licenses, current]
Created = 2011-12-15T20:50:49Z
Publication Date = 2016-08-30T22:01:35Z
Rows Updated = 2017-02-22T10:56:22Z
```

[description]
```ls
<div style="height:350px; overflow-y: scroll;">
                                            This dataset contains all current and active business licenses issued by the Department of Business Affairs and Consumer Protection.  This dataset contains a large number of records /rows of data and may not be viewed in full in Microsoft Excel.  Therefore, when downloading the file, select CSV from the Export menu.  Open the file in an ASCII text editor, such as Notepad or Wordpad, to view and search.  

 
Data fields requiring description are detailed below. 

APPLICATION TYPE:  'ISSUE' is the record associated with the initial license application.  'RENEW' is a subsequent renewal record.  All renewal records are created with a term start date and term expiration date.  'C_LOC' is a change of location record.  It means the business moved.   'C_CAPA' is a change of capacity record.  Only a few license types my file this type of application.  'C_EXPA' only applies to businesses that have liquor licenses.  It means the business location expanded.

LICENSE STATUS: 'AAI' means the license was issued. 

Business license owners may be accessed at:  http://data.cityofchicago.org/Community-Economic-Development/Business-Owners/ezma-pppn
To identify the owner of a business, you will need the account number or legal name.

 
Data Owner: Business Affairs and Consumer Protection 
 
Time Period: Current 
 
Frequency: Data is updated daily</div>
```

[columns]
```ls
| Name                              | Field Name                        | Data Type     | Render Type   | Schema Type    | Included | 
| ================================= | ================================= | ============= | ============= | ============== | ======== | 
| ID                                | id                                | text          | text          |                | No       | 
| LICENSE ID                        | license_id                        | text          | number        | series tag     | Yes      | 
| ACCOUNT NUMBER                    | account_number                    | text          | number        | series tag     | Yes      | 
| SITE NUMBER                       | site_number                       | text          | number        | series tag     | Yes      | 
| LEGAL NAME                        | legal_name                        | text          | text          | series tag     | Yes      | 
| DOING BUSINESS AS NAME            | doing_business_as_name            | text          | text          | series tag     | Yes      | 
| ADDRESS                           | address                           | text          | text          |                | No       | 
| CITY                              | city                              | text          | text          | series tag     | Yes      | 
| STATE                             | state                             | text          | text          | series tag     | Yes      | 
| ZIP CODE                          | zip_code                          | text          | text          | series tag     | Yes      | 
| WARD                              | ward                              | text          | number        | series tag     | Yes      | 
| PRECINCT                          | precinct                          | text          | number        | series tag     | Yes      | 
| WARD PRECINCT                     | ward_precinct                     | text          | text          | series tag     | Yes      | 
| POLICE DISTRICT                   | police_district                   | text          | number        | series tag     | Yes      | 
| LICENSE CODE                      | license_code                      | text          | number        | series tag     | Yes      | 
| LICENSE DESCRIPTION               | license_description               | text          | text          | series tag     | Yes      | 
| BUSINESS ACTIVITY ID              | business_activity_id              | text          | text          | series tag     | Yes      | 
| BUSINESS ACTIVITY                 | business_activity                 | text          | text          | series tag     | Yes      | 
| LICENSE NUMBER                    | license_number                    | text          | number        | series tag     | Yes      | 
| APPLICATION TYPE                  | application_type                  | text          | text          | series tag     | Yes      | 
| APPLICATION CREATED DATE          | application_created_date          | calendar_date | calendar_date | time           | Yes      | 
| APPLICATION REQUIREMENTS COMPLETE | application_requirements_complete | calendar_date | calendar_date |                | No       | 
| PAYMENT DATE                      | payment_date                      | calendar_date | calendar_date |                | No       | 
| CONDITIONAL APPROVAL              | conditional_approval              | number        | text          | numeric metric | Yes      | 
| LICENSE TERM START DATE           | license_start_date                | calendar_date | calendar_date |                | No       | 
| LICENSE TERM EXPIRATION DATE      | expiration_date                   | calendar_date | calendar_date |                | No       | 
| LICENSE APPROVED FOR ISSUANCE     | license_approved_for_issuance     | calendar_date | calendar_date |                | No       | 
| DATE ISSUED                       | date_issued                       | calendar_date | calendar_date |                | No       | 
| LICENSE STATUS                    | license_status                    | text          | text          | series tag     | Yes      | 
| LICENSE STATUS CHANGE DATE        | license_status_change_date        | calendar_date | calendar_date |                | No       | 
| SSA                               | ssa                               | number        | text          | numeric metric | Yes      | 
| LATITUDE                          | latitude                          | number        | number        |                | No       | 
| LONGITUDE                         | longitude                         | number        | number        |                | No       | 
```

[time]
```ls
Value = application_created_date
Format & Zone = yyyy-MM-dd'T'HH:mm:ss
```

[series]
```ls
Metric Prefix = 
Included Fields = *
Excluded Fields = id,license_approved_for_issuance,license_start_date,payment_date,application_requirements_complete,address,expiration_date,license_status_change_date,longitude,latitude,date_issued
Annotation Fields = 
```

[commands]
```ls
series e:uupf-x98q d:2013-08-14T00:00:00.000Z t:license_code=1003 t:application_type=ISSUE t:business_activity_id=725 t:license_status=AAI t:account_number=305598 t:police_district=10 t:zip_code=60623 t:license_description="Public Garage" t:license_number=2279620 t:state=IL t:site_number=17 t:business_activity="Provide Parking Spaces For a Fee -Available and Advertised to the Public" t:ward_precinct=12-1 t:city=CHICAGO t:precinct=1 t:legal_name="PREFER VALET PARKING SERVICE, INC." t:ward=12 t:doing_business_as_name="PREFER VALET PARKING SERVICES, INC." t:conditional_approval=N t:license_id=2279620 m:ssa=25

series e:uupf-x98q d:2016-08-04T00:00:00.000Z t:license_code=1006 t:application_type=C_LOC t:business_activity_id=782 t:license_status=AAI t:account_number=316966 t:police_district=18 t:zip_code=60610 t:license_description="Retail Food Establishment" t:license_number=2483710 t:state=IL t:site_number=3 t:business_activity="Sale of Food Prepared Onsite Without Dining Area" t:ward_precinct=27-54 t:city=CHICAGO t:precinct=54 t:legal_name="OLD TOWN OIL, LLC" t:ward=27 t:doing_business_as_name="OLD TOWN OIL, LLC" t:conditional_approval=N t:license_id=2483710 m:ssa=48

series e:uupf-x98q d:2017-02-22T14:56:35.171Z t:license_code=1010 t:application_type=RENEW t:business_activity_id=708 t:license_status=AAI t:account_number=352159 t:zip_code=60601 t:license_description="Limited Business License" t:license_number=2374115 t:state=IL t:site_number=3 t:business_activity="Miscellaneous Commercial Services" t:ward_precinct=42- t:city=CHICAGO t:legal_name="Genesys Works Chicago" t:ward=42 t:doing_business_as_name="Genesys Works Chicago" t:conditional_approval=N t:license_id=2481313 m:ssa=1

```

[meta-commands]
```ls
metric m:ssa p:integer l:SSA d:"Special Service Areas are local tax districts that fund expanded services and programs, to foster commercial and economic development, through a localized property tax. In other cities these areas are sometimes called Business Improvement Districts (BIDs). This portal contains a map of all Chicago SSAs" t:dataTypeName=number

entity e:uupf-x98q l:"Business Licenses - Current Active" t:attribution="City of Chicago" t:url=https://data.cityofchicago.org/api/views/uupf-x98q

property e:uupf-x98q t:meta.view d:2017-02-22T14:56:35.171Z v:id=uupf-x98q v:category="Community & Economic Development" v:attributionLink=http://www.cityofchicago.org v:averageRating=0 v:name="Business Licenses - Current Active" v:attribution="City of Chicago"

property e:uupf-x98q t:meta.view.owner d:2017-02-22T14:56:35.171Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

property e:uupf-x98q t:meta.view.tableauthor d:2017-02-22T14:56:35.171Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

```
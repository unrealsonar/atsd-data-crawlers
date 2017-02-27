[dataset]
```property
URL = https://data.cityofchicago.org/api/views/ydr8-5enu
Catalog URL = https://catalog.data.gov/dataset/building-permits
Id = ydr8-5enu
Name = Building Permits
Attribution = City of Chicago
Attribution Link = http://www.cityofchicago.org
Category = Buildings
Tags = [permits]
Created = 2011-09-30T12:00:08Z
Publication Date = 2015-07-07T06:44:45Z
Rows Updated = 2017-02-22T13:08:42Z
```

[description]
```ls
<div style="height:350px; overflow-y: scroll;">
                                            Permits issued by the Department of Buildings in the City of Chicago from 2006 to the present. The dataset for each year contains more than 65,000 records/rows of data and cannot be viewed in full in Microsoft Excel. Therefore, when downloading the file, select CSV from the Export menu. Open the file in an ASCII text editor, such as Wordpad, to view and search. Data fields requiring description are detailed below. 
PERMIT TYPE: "New Construction and Renovation" includes new projects or rehabilitations of existing buildings; "Other Construction" includes items that require plans such as cell towers and cranes; "Easy Permit" includes minor repairs that require no plans; "Wrecking/Demolition" includes private demolition of buildings and other structures; "Electrical Wiring" includes major and minor electrical work both permanent and temporary; "Sign Permit" includes signs, canopies and awnings both on private property and over the public way; "Porch Permit" includes new porch construction and renovation (defunct permit type porches are now issued under "New Construction and Renovation" directly); "Reinstate Permit" includes original permit reinstatements; "Extension Permits" includes extension of original permit when construction has not started within six months of original permit issuance. WORK DESCRIPTION: The description of work being done on the issued permit, which is printed on the permit. PIN1 ? PIN10: A maximum of ten assessor parcel index numbers belonging to the permitted property. PINs are provided by the customer seeking the permit since mid-2008 where required by the Cook County Assessor?s Office. CONTRACTOR INFORMATION: The contractor type, name, and contact information. Data includes up to 15 different contractors per permit if applicable.

Data Owner: Buildings.

Time Period: January 1, 2006 to present.

Frequency: Data is updated daily.

Related Applications: Building Data Warehouse (https://webapps.cityofchicago.org/buildingviolations/violations/searchaddresspage.html).</div>
```

[columns]
```ls
| Name                  | Field Name            | Data Type     | Render Type   | Schema Type    | Included | 
| ===================== | ===================== | ============= | ============= | ============== | ======== | 
| ID                    | id                    | text          | text          |                | No       | 
| PERMIT#               | permit_               | text          | text          | series tag     | Yes      | 
| PERMIT_TYPE           | _permit_type          | text          | text          | series tag     | Yes      | 
| ISSUE_DATE            | _issue_date           | calendar_date | calendar_date | time           | Yes      | 
| ESTIMATED_COST        | _estimated_cost       | money         | money         | numeric metric | Yes      | 
| AMOUNT_WAIVED         | _amount_waived        | money         | money         | numeric metric | Yes      | 
| AMOUNT_PAID           | _amount_paid          | money         | money         | numeric metric | Yes      | 
| TOTAL_FEE             | _total_permit_fee     | money         | money         | numeric metric | Yes      | 
| STREET_NUMBER         | street_number         | text          | number        | series tag     | Yes      | 
| STREET DIRECTION      | street_direction      | text          | text          | series tag     | Yes      | 
| STREET_NAME           | street_name           | text          | text          | series tag     | Yes      | 
| SUFFIX                | _suffix               | text          | text          | series tag     | Yes      | 
| WORK_DESCRIPTION      | work_description      | text          | text          | series tag     | Yes      | 
| PIN1                  | _pin1                 | text          | text          | series tag     | Yes      | 
| PIN2                  | _pin2                 | text          | text          | series tag     | Yes      | 
| PIN3                  | _pin3                 | text          | text          | series tag     | Yes      | 
| PIN4                  | _pin4                 | text          | text          | series tag     | Yes      | 
| PIN5                  | _pin5                 | text          | text          | series tag     | Yes      | 
| PIN6                  | _pin6                 | text          | text          | series tag     | Yes      | 
| PIN7                  | _pin7                 | text          | text          | series tag     | Yes      | 
| PIN8                  | _pin8                 | text          | text          | series tag     | Yes      | 
| PIN9                  | _pin9                 | text          | text          | series tag     | Yes      | 
| PIN10                 | _pin10                | text          | text          | series tag     | Yes      | 
| CONTRACTOR_1_TYPE     | contractor_1_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_1_NAME     | contractor_1_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_1_ADDRESS  | contractor_1_address  | text          | text          |                | No       | 
| CONTRACTOR_1_CITY     | contractor_1_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_1_STATE    | contractor_1_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_1_ZIPCODE  | contractor_1_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_1_PHONE    | contractor_1_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_2_TYPE     | contractor_2_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_2_NAME     | contractor_2_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_2_ADDRESS  | contractor_2_address  | text          | text          |                | No       | 
| CONTRACTOR_2_CITY     | contractor_2_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_2_STATE    | contractor_2_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_2_ZIPCODE  | contractor_2_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_2_PHONE    | contractor_2_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_3_TYPE     | contractor_3_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_3_NAME     | contractor_3_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_3_ADDRESS  | contractor_3_address  | text          | text          |                | No       | 
| CONTRACTOR_3_CITY     | contractor_3_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_3_STATE    | contractor_3_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_3_ZIPCODE  | contractor_3_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_3_PHONE    | contractor_3_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_4_TYPE     | contractor_4_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_4_NAME     | contractor_4_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_4_ADDRESS  | contractor_4_address  | text          | text          |                | No       | 
| CONTRACTOR_4_CITY     | contractor_4_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_4_STATE    | contractor_4_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_4_ZIPCODE  | contractor_4_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_4_PHONE    | contractor_4_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_5_TYPE     | contractor_5_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_5_NAME     | contractor_5_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_5_ADDRESS  | contractor_5_address  | text          | text          |                | No       | 
| CONTRACTOR_5_CITY     | contractor_5_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_5_STATE    | contractor_5_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_5_ZIPCODE  | contractor_5_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_5_PHONE    | contractor_5_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_6_TYPE     | contractor_6_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_6_NAME     | contractor_6_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_6_ADDRESS  | contractor_6_address  | text          | text          |                | No       | 
| CONTRACTOR_6_CITY     | contractor_6_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_6_STATE    | contractor_6_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_6_ZIPCODE  | contractor_6_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_6_PHONE    | contractor_6_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_7_TYPE     | contractor_7_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_7_NAME     | contractor_7_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_7_ADDRESS  | contractor_7_address  | text          | text          |                | No       | 
| CONTRACTOR_7_CITY     | contractor_7_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_7_STATE    | contractor_7_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_7_ZIPCODE  | contractor_7_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_7_PHONE    | contractor_7_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_8_TYPE     | contractor_8_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_8_NAME     | contractor_8_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_8_ADDRESS  | contractor_8_address  | text          | text          |                | No       | 
| CONTRACTOR_8_CITY     | contractor_8_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_8_STATE    | contractor_8_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_8_ZIPCODE  | contractor_8_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_8_PHONE    | contractor_8_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_9_TYPE     | contractor_9_type     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_9_NAME     | contractor_9_name     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_9_ADDRESS  | contractor_9_address  | text          | text          |                | No       | 
| CONTRACTOR_9_CITY     | contractor_9_city     | text          | text          | series tag     | Yes      | 
| CONTRACTOR_9_STATE    | contractor_9_state    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_9_ZIPCODE  | contractor_9_zipcode  | text          | text          | series tag     | Yes      | 
| CONTRACTOR_9_PHONE    | contractor_9_phone    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_10_TYPE    | contractor_10_type    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_10_NAME    | contractor_10_name    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_10_ADDRESS | contractor_10_address | text          | text          |                | No       | 
| CONTRACTOR_10_CITY    | contractor_10_city    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_10_STATE   | contractor_10_state   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_10_ZIPCODE | contractor_10_zipcode | text          | text          | series tag     | Yes      | 
| CONTRACTOR_10_PHONE   | contractor_10_phone   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_11_TYPE    | contractor_11_type    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_11_NAME    | contractor_11_name    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_11_ADDRESS | contractor_11_address | text          | text          |                | No       | 
| CONTRACTOR_11_CITY    | contractor_11_city    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_11_STATE   | contractor_11_state   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_11_ZIPCODE | contractor_11_zipcode | text          | text          | series tag     | Yes      | 
| CONTRACTOR_11_PHONE   | contractor_11_phone   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_12_TYPE    | contractor_12_type    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_12_NAME    | contractor_12_name    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_12_ADDRESS | contractor_12_address | text          | text          |                | No       | 
| CONTRACTOR_12_CITY    | contractor_12_city    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_12_STATE   | contractor_12_state   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_12_ZIPCODE | contractor_12_zipcode | text          | text          | series tag     | Yes      | 
| CONTRACTOR_12_PHONE   | contractor_12_phone   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_13_TYPE    | contractor_13_type    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_13_NAME    | contractor_13_name    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_13_ADDRESS | contractor_13_address | text          | text          |                | No       | 
| CONTRACTOR_13_CITY    | contractor_13_city    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_13_STATE   | contractor_13_state   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_13_ZIPCODE | contractor_13_zipcode | text          | text          | series tag     | Yes      | 
| CONTRACTOR_13_PHONE   | contractor_13_phone   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_14_TYPE    | contractor_14_type    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_14_NAME    | contractor_14_name    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_14_ADDRESS | contractor_14_address | text          | text          |                | No       | 
| CONTRACTOR_14_CITY    | contractor_14_city    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_14_STATE   | contractor_14_state   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_14_ZIPCODE | contractor_14_zipcode | text          | text          | series tag     | Yes      | 
| CONTRACTOR_14_PHONE   | contractor_14_phone   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_15_TYPE    | contractor_15_type    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_15_NAME    | contractor_15_name    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_15_ADDRESS | contractor_15_address | text          | text          |                | No       | 
| CONTRACTOR_15_CITY    | contractor_15_city    | text          | text          | series tag     | Yes      | 
| CONTRACTOR_15_STATE   | contractor_15_state   | text          | text          | series tag     | Yes      | 
| CONTRACTOR_15_ZIPCODE | contractor_15_zipcode | text          | text          | series tag     | Yes      | 
| CONTRACTOR_15_PHONE   | contractor_15_phone   | text          | text          | series tag     | Yes      | 
| LATITUDE              | latitude              | number        | number        |                | No       | 
| LONGITUDE             | longitude             | number        | number        |                | No       | 
```

[time]
```ls
Value = _issue_date
Format & Zone = yyyy-MM-dd'T'HH:mm:ss
```

[series]
```ls
Metric Prefix = 
Included Fields = *
Excluded Fields = contractor_14_address,contractor_1_address,contractor_10_address,contractor_7_address,id,contractor_8_address,contractor_15_address,contractor_6_address,contractor_12_address,contractor_2_address,longitude,latitude,contractor_3_address,contractor_9_address,contractor_13_address,contractor_11_address,contractor_5_address,contractor_4_address
Annotation Fields = 
```

[commands]
```ls
series e:ydr8-5enu d:2016-03-22T00:00:00.000Z t:contractor_6_zipcode=60609- t:street_name=INDIANA t:contractor_1_name="WOZNY ZENON" t:contractor_10_zipcode=60467- t:contractor_8_state=IL t:contractor_2_zipcode=60616 t:contractor_10_name="JOSEPH SKIBA" t:contractor_6_name="KASPER DEVELOPMENT, LTD." t:contractor_5_state=IL t:contractor_6_city=CHICAGO t:contractor_7_zipcode=60616- t:permit_=100588856 t:contractor_5_name="LOLO CONSTRUCTION, INC." t:contractor_10_state=IL t:contractor_4_city=CHICAGO t:contractor_2_phone="(312)907-7273 X" t:contractor_10_type=OWNER t:contractor_3_city=CHICAGO t:street_direction=S t:contractor_9_zipcode=60480- t:contractor_3_phone="(773)802-9937 x" t:contractor_2_city=CHICAGO t:_permit_type="PERMIT - NEW CONSTRUCTION" t:contractor_7_city=CHICAGO t:contractor_8_type=CONTRACTOR-REFRIGERATION t:contractor_5_city=CHICAGO t:contractor_7_state=IL t:contractor_4_name="KASPER DEVELOPMENT LTD." t:contractor_10_city="ORLAND PARK" t:contractor_8_zipcode=60480- t:contractor_1_type=ARCHITECT t:work_description="ERECT A (4A CONST) FRAME 2 STORY SINGLE FAMILY HOME WITH A DETACHED FRAME 2 CAR GARAGE AS PER PLANS (CONDITIONAL PERMIT SUBJECT TO FIELD INSPECTIONS)" t:contractor_7_type=CONTRACTOR-PLUMBER/PLUMBING t:contractor_8_city="WILLOW SPRINGS" t:contractor_7_phone="(312)791-0097 x" t:contractor_5_phone=(708)341-6942 t:contractor_2_state=IL t:contractor_2_type=CONTRACTOR-ELECTRICAL t:contractor_5_zipcode=60609 t:street_number=4827 t:contractor_4_phone=(773)633-0087 t:contractor_7_name="M. DIFOGGIO PLUMBING & SEWER" t:contractor_6_state=IL t:contractor_3_name="STERN GROUP CORPORATION" t:_suffix=AVE t:contractor_4_type="CONTRACTOR-GENERAL CONTRACTOR" t:contractor_3_state=IL t:contractor_8_name="K & B HEATING & COOLING" t:contractor_3_zipcode=60631- t:contractor_9_name="K & B HEATING & COOLING" t:contractor_4_zipcode=60609- t:contractor_9_type=CONTRACTOR-VENTILATION t:contractor_4_state=IL t:contractor_9_state=IL t:contractor_5_type="MASONRY CONTRACTOR" t:contractor_3_type=EXPEDITOR t:contractor_9_city="WILLOW SPRINGS" t:contractor_2_name="KUO CONSTRUCTION, INC." t:contractor_6_type=OWNER m:_estimated_cost=200000 m:_total_permit_fee=800 m:_amount_paid=800 m:_amount_waived=0

series e:ydr8-5enu d:2016-03-22T00:00:00.000Z t:contractor_1_state=IL t:_suffix=AVE t:contractor_1_phone="(773)489-5387 x" t:permit_=100592829 t:street_name=CORNELIA t:_pin1=14-20-409-029-0000 t:contractor_1_name="TAYLOR EXCAVATING/CONS" t:contractor_1_type=CONTRACTOR-WRECKING t:work_description="WRECK AND REMOVE A DETACHED FRAME RESIDENCE- FRONT BUILDING" t:contractor_1_city=CHGO. t:contractor_2_zipcode=60618 t:contractor_1_zipcode=60642- t:contractor_2_state=IL t:contractor_2_type=OWNER t:street_direction=W t:street_number=1051 t:contractor_2_city=CHICAGO t:contractor_2_name="1051 W CORNELIA LLC" t:_permit_type="PERMIT - WRECKING/DEMOLITION" m:_estimated_cost=1 m:_total_permit_fee=500 m:_amount_paid=500 m:_amount_waived=0

series e:ydr8-5enu d:2016-03-22T00:00:00.000Z t:contractor_1_state=IL t:_suffix=AVE t:contractor_1_phone="(773)489-5387 x" t:permit_=100592831 t:street_name=CORNELIA t:_pin1=14-20-409-029-0000 t:contractor_1_name="TAYLOR EXCAVATING/CONS" t:contractor_1_type=CONTRACTOR-WRECKING t:work_description="WRECK AND REMOVE A DETACHED FRAME COACH HOUSE AND DETACHED FRAME GARAGE IN REAR" t:contractor_1_city=CHGO. t:contractor_2_zipcode=60618 t:contractor_1_zipcode=60642- t:contractor_2_state=IL t:contractor_2_type=OWNER t:street_direction=W t:street_number=1051 t:contractor_2_city=CHICAGO t:contractor_2_name="1051 W CORNELIA LLC" t:_permit_type="PERMIT - WRECKING/DEMOLITION" m:_estimated_cost=1 m:_total_permit_fee=500 m:_amount_paid=500 m:_amount_waived=0

```

[meta-commands]
```ls
entity e:ydr8-5enu l:"Building Permits" t:attribution="City of Chicago" t:url=https://data.cityofchicago.org/api/views/ydr8-5enu

property e:ydr8-5enu t:meta.view d:2017-02-22T14:57:07.515Z v:id=ydr8-5enu v:category=Buildings v:attributionLink=http://www.cityofchicago.org v:averageRating=0 v:name="Building Permits" v:attribution="City of Chicago"

property e:ydr8-5enu t:meta.view.owner d:2017-02-22T14:57:07.515Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

property e:ydr8-5enu t:meta.view.tableauthor d:2017-02-22T14:57:07.515Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

```
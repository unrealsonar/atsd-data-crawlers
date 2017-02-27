[dataset]
```property
URL = https://data.sfgov.org/api/views/rkru-6vcg
Catalog URL = https://catalog.data.gov/dataset/air-traffic-passenger-statistics
Id = rkru-6vcg
Name = Air Traffic Passenger Statistics
Description = San Francisco International Airport Report on Monthly Passenger Traffic Statistics by Airline. Airport data is seasonal in nature, therefore any comparative analyses should be done on a period-over...
Category = Transportation
Tags = [passengers, airport, travel]
Created = 2016-04-19T23:51:23Z
Publication Date = 2017-01-11T19:39:43Z
Rows Updated = 2017-01-11T19:38:42Z
```

[description]
```ls

```

[columns]
```ls
| Name                        | Field Name                  | Data Type | Render Type | Schema Type    | Included | 
| =========================== | =========================== | ========= | =========== | ============== | ======== | 
| Activity Period             | activity_period             | number    | number      | time           | Yes      | 
| Operating Airline           | operating_airline           | text      | text        | series tag     | Yes      | 
| Operating Airline IATA Code | operating_airline_iata_code | text      | text        | series tag     | Yes      | 
| Published Airline           | published_airline           | text      | text        | series tag     | Yes      | 
| Published Airline IATA Code | published_airline_iata_code | text      | text        | series tag     | Yes      | 
| GEO Summary                 | geo_summary                 | text      | text        | series tag     | Yes      | 
| GEO Region                  | geo_region                  | text      | text        | series tag     | Yes      | 
| Activity Type Code          | activity_type_code          | text      | text        | series tag     | Yes      | 
| Price Category Code         | price_category_code         | text      | text        | series tag     | Yes      | 
| Terminal                    | terminal                    | text      | text        | series tag     | Yes      | 
| Boarding Area               | boarding_area               | text      | text        | series tag     | Yes      | 
| Passenger Count             | passenger_count             | number    | number      | numeric metric | Yes      | 
```

[time]
```ls
Value = activity_period
Format & Zone = yyyyMM
```

[series]
```ls
Metric Prefix = 
Included Fields = *
Excluded Fields = 
Annotation Fields = 
```

[commands]
```ls
series e:rkru-6vcg d:2005-07-01T00:00:00.000Z t:activity_type_code=Deplaned t:boarding_area=B t:terminal="Terminal 1" t:geo_summary=Domestic t:geo_region=US t:operating_airline_iata_code=TZ t:published_airline="ATA Airlines" t:published_airline_iata_code=TZ t:operating_airline="ATA Airlines" t:price_category_code="Low Fare" m:passenger_count=27271

series e:rkru-6vcg d:2005-07-01T00:00:00.000Z t:activity_type_code=Enplaned t:boarding_area=B t:terminal="Terminal 1" t:geo_summary=Domestic t:geo_region=US t:operating_airline_iata_code=TZ t:published_airline="ATA Airlines" t:published_airline_iata_code=TZ t:operating_airline="ATA Airlines" t:price_category_code="Low Fare" m:passenger_count=29131

series e:rkru-6vcg d:2005-07-01T00:00:00.000Z t:activity_type_code="Thru / Transit" t:boarding_area=B t:terminal="Terminal 1" t:geo_summary=Domestic t:geo_region=US t:operating_airline_iata_code=TZ t:published_airline="ATA Airlines" t:published_airline_iata_code=TZ t:operating_airline="ATA Airlines" t:price_category_code="Low Fare" m:passenger_count=5415

```

[meta-commands]
```ls
metric m:passenger_count p:integer l:"Passenger Count" t:dataTypeName=number

entity e:rkru-6vcg l:"Air Traffic Passenger Statistics" t:url=https://data.sfgov.org/api/views/rkru-6vcg

property e:rkru-6vcg t:meta.view d:2017-02-22T14:41:21.660Z v:id=rkru-6vcg v:category=Transportation v:averageRating=0 v:name="Air Traffic Passenger Statistics"

property e:rkru-6vcg t:meta.view.license d:2017-02-22T14:41:21.660Z v:name="Open Data Commons Public Domain Dedication and License" v:termsLink=http://opendatacommons.org/licenses/pddl/1.0/

property e:rkru-6vcg t:meta.view.owner d:2017-02-22T14:41:21.660Z v:id=dbag-6qd9 v:screenName=OpenData v:roleName=publisher v:displayName=OpenData

property e:rkru-6vcg t:meta.view.tableauthor d:2017-02-22T14:41:21.660Z v:id=dbag-6qd9 v:screenName=OpenData v:roleName=publisher v:displayName=OpenData

```
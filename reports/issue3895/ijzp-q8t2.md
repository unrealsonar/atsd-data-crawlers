[dataset]
```property
URL = https://data.cityofchicago.org/api/views/ijzp-q8t2
Catalog URL = https://catalog.data.gov/dataset/crimes-2001-to-present-398a4
Id = ijzp-q8t2
Name = Crimes - 2001 to present
Attribution = Chicago Police Department
Attribution Link = https://portal.chicagopolice.org/portal/page/portal/ClearPath
Category = Public Safety
Tags = [crime, police]
Created = 2011-09-30T17:59:31Z
Publication Date = 2016-06-08T21:22:41Z
Rows Updated = 2017-02-22T11:39:41Z
```

[description]
```ls

```

[columns]
```ls
| Name                 | Field Name           | Data Type     | Render Type   | Schema Type | Included | 
| ==================== | ==================== | ============= | ============= | =========== | ======== | 
| ID                   | id                   | text          | number        |             | No       | 
| Case Number          | case_number          | text          | text          | series tag  | Yes      | 
| Date                 | date                 | calendar_date | calendar_date | time        | Yes      | 
| Block                | block                | text          | text          | series tag  | Yes      | 
| IUCR                 | iucr                 | text          | text          | series tag  | Yes      | 
| Primary Type         | primary_type         | text          | text          | series tag  | Yes      | 
| Description          | description          | text          | text          | series tag  | Yes      | 
| Location Description | location_description | text          | text          | series tag  | Yes      | 
| Arrest               | arrest               | checkbox      | checkbox      | series tag  | Yes      | 
| Domestic             | domestic             | checkbox      | checkbox      | series tag  | Yes      | 
| Beat                 | beat                 | text          | text          | series tag  | Yes      | 
| District             | district             | text          | text          | series tag  | Yes      | 
| Ward                 | ward                 | text          | number        | series tag  | Yes      | 
| Community Area       | community_area       | text          | text          | series tag  | Yes      | 
| FBI Code             | fbi_code             | text          | text          | series tag  | Yes      | 
| X Coordinate         | x_coordinate         | number        | number        |             | No       | 
| Y Coordinate         | y_coordinate         | number        | number        |             | No       | 
| Year                 | year                 | number        | number        |             | No       | 
| Updated On           | updated_on           | calendar_date | calendar_date |             | No       | 
| Latitude             | latitude             | number        | number        |             | No       | 
| Longitude            | longitude            | number        | number        |             | No       | 
```

[time]
```ls
Value = date
Format & Zone = yyyy-MM-dd'T'HH:mm:ss
```

[series]
```ls
Metric Prefix = 
Included Fields = *
Excluded Fields = id,y_coordinate,updated_on,x_coordinate,year,longitude,latitude
Annotation Fields = 
```

[commands]
```ls
```

[meta-commands]
```ls
entity e:ijzp-q8t2 l:"Crimes - 2001 to present" t:attribution="Chicago Police Department" t:url=https://data.cityofchicago.org/api/views/ijzp-q8t2

property e:ijzp-q8t2 t:meta.view d:2017-02-22T15:19:48.261Z v:id=ijzp-q8t2 v:category="Public Safety" v:attributionLink=https://portal.chicagopolice.org/portal/page/portal/ClearPath v:averageRating=0 v:name="Crimes - 2001 to present" v:attribution="Chicago Police Department"

property e:ijzp-q8t2 t:meta.view.owner d:2017-02-22T15:19:48.261Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

property e:ijzp-q8t2 t:meta.view.tableauthor d:2017-02-22T15:19:48.261Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

```
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
Rows Updated = 2017-02-26T11:46:56Z
```

[description]
```ls
This dataset reflects reported incidents of crime (with the exception of murders where data exists for each victim) that occurred in the City of Chicago from 2001 to present, minus the most recent seven days. Data is extracted from the Chicago Police Department's CLEAR (Citizen Law Enforcement Analysis and Reporting) system. In order to protect the privacy of crime victims, addresses are shown at the block level only and specific locations are not identified. Should you have questions about this dataset, you may contact the Research & Development Division of the Chicago Police Department at 312.745.6071 or RDAnalysis@chicagopolice.org.  Disclaimer: These crimes may be based upon preliminary information supplied to the Police Department by the reporting parties that have not been verified. The preliminary crime classifications may be changed at a later date based upon additional investigation and there is always the possibility of mechanical or human error. Therefore, the Chicago Police Department does not guarantee (either expressed or implied) the accuracy, completeness, timeliness, or correct sequencing of the information and the information should not be used for comparison purposes over time. The Chicago Police Department will not be responsible for any error or omission, or for the use of, or the results obtained from the use of this information. All data visualizations on maps should be considered approximate and attempts to derive specific addresses are strictly prohibited. The Chicago Police Department is not responsible for the content of any off-site pages that are referenced by or that reference this web page other than an official City of Chicago or Chicago Police Department web page. The user specifically acknowledges that the Chicago Police Department is not responsible for any defamatory, offensive, misleading, or illegal conduct of other users, links, or third parties and that the risk of injury from the foregoing rests entirely with the user.  The unauthorized use of the words "Chicago Police Department," "Chicago Police," or any colorable imitation of these words or the unauthorized use of the Chicago Police Department logo is unlawful. This web page does not, in any way, authorize such use. Data are updated daily. The dataset contains more than 65,000 records/rows of data and cannot be viewed in full in Microsoft Excel. Therefore, when downloading the file, select CSV from the Export menu. Open the file in an ASCII text editor, such as Wordpad, to view and search. To access a list of Chicago Police Department - Illinois Uniform Crime Reporting (IUCR) codes, go to http://data.cityofchicago.org/Public-Safety/Chicago-Police-Department-Illinois-Uniform-Crime-R/c7ck-438e
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

property e:ijzp-q8t2 t:meta.view d:2017-02-27T08:56:45.704Z v:id=ijzp-q8t2 v:category="Public Safety" v:attributionLink=https://portal.chicagopolice.org/portal/page/portal/ClearPath v:averageRating=0 v:name="Crimes - 2001 to present" v:attribution="Chicago Police Department"

property e:ijzp-q8t2 t:meta.view.owner d:2017-02-27T08:56:45.704Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

property e:ijzp-q8t2 t:meta.view.tableauthor d:2017-02-27T08:56:45.704Z v:id=scy9-9wg4 v:profileImageUrlMedium=/api/users/scy9-9wg4/profile_images/THUMB v:profileImageUrlLarge=/api/users/scy9-9wg4/profile_images/LARGE v:screenName=cocadmin v:profileImageUrlSmall=/api/users/scy9-9wg4/profile_images/TINY v:roleName=administrator v:displayName=cocadmin v:privilegesDisabled=false

```
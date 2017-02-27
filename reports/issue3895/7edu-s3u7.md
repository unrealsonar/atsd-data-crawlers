[dataset]
```property
URL = https://data.cityofchicago.org/api/views/7edu-s3u7
Catalog URL = 
Id = 7edu-s3u7
Name = Beach Weather Stations - Automated Sensors - 2016 - Humidity
Attribution = Chicago Park District
Attribution Link = http://www.chicagoparkdistrict.com
Category = Parks & Recreation
Tags = [beaches, parks, chicago park district, open spaces, parks & recreation, recreation, water]
Created = 2016-06-07T16:41:13Z
Publication Date = 2015-06-01T22:22:24Z
Rows Updated = 2017-02-22T14:45:10Z
```

[description]
```ls

```

[columns]
```ls
| Name                        | Field Name                  | Data Type     | Render Type   | Schema Type    | Included | 
| =========================== | =========================== | ============= | ============= | ============== | ======== | 
| Station Name                | station_name                | text          | text          | series tag     | Yes      | 
| Measurement Timestamp       | measurement_timestamp       | calendar_date | calendar_date | time           | Yes      | 
| Air Temperature             | air_temperature             | number        | number        | numeric metric | Yes      | 
| Wet Bulb Temperature        | wet_bulb_temperature        | number        | number        | numeric metric | Yes      | 
| Humidity                    | humidity                    | number        | number        | numeric metric | Yes      | 
| Rain Intensity              | rain_intensity              | number        | number        | numeric metric | Yes      | 
| Interval Rain               | interval_rain               | number        | number        | numeric metric | Yes      | 
| Total Rain                  | total_rain                  | number        | number        | numeric metric | Yes      | 
| Precipitation Type          | precipitation_type          | number        | number        | numeric metric | Yes      | 
| Wind Direction              | wind_direction              | number        | number        | numeric metric | Yes      | 
| Wind Speed                  | wind_speed                  | number        | number        | numeric metric | Yes      | 
| Maximum Wind Speed          | maximum_wind_speed          | number        | number        | numeric metric | Yes      | 
| Barometric Pressure         | barometric_pressure         | number        | number        | numeric metric | Yes      | 
| Solar Radiation             | solar_radiation             | number        | number        | numeric metric | Yes      | 
| Heading                     | heading                     | number        | number        | numeric metric | Yes      | 
| Battery Life                | battery_life                | number        | number        | numeric metric | Yes      | 
| Measurement Timestamp Label | measurement_timestamp_label | text          | text          |                | No       | 
| Measurement ID              | measurement_id              | text          | text          |                | No       | 
```

[time]
```ls
Value = measurement_timestamp
Format & Zone = yyyy-MM-dd'T'HH:mm:ss
```

[series]
```ls
Metric Prefix = 
Included Fields = *
Excluded Fields = measurement_id,measurement_timestamp_label
Annotation Fields = 
```

[commands]
```ls
series e:7edu-s3u7 d:2016-01-01T00:00:00.000Z t:station_name="Foster Weather Station" m:barometric_pressure=1000 m:humidity=62 m:wind_direction=197 m:interval_rain=0 m:maximum_wind_speed=5 m:solar_radiation=0 m:wind_speed=3.6 m:battery_life=15.2 m:air_temperature=-3.67

series e:7edu-s3u7 d:2016-01-01T00:00:00.000Z t:station_name="Oak Street Weather Station" m:wind_direction=260 m:interval_rain=0 m:solar_radiation=3 m:wind_speed=4 m:wet_bulb_temperature=-4 m:air_temperature=-2.3 m:barometric_pressure=1000.5 m:humidity=67 m:precipitation_type=0 m:total_rain=6.3 m:maximum_wind_speed=7.6 m:rain_intensity=0 m:heading=359 m:battery_life=12.1

series e:7edu-s3u7 d:2016-01-01T00:00:00.000Z t:station_name="63rd Street Weather Station" m:wind_direction=255 m:interval_rain=0 m:solar_radiation=5 m:wind_speed=3.8 m:wet_bulb_temperature=-4.4 m:air_temperature=-2.8 m:barometric_pressure=1000.5 m:humidity=69 m:precipitation_type=0 m:total_rain=6.7 m:maximum_wind_speed=6.4 m:rain_intensity=0 m:heading=353 m:battery_life=11.9

```

[meta-commands]
```ls
metric m:air_temperature l:"Air Temperature" d:"Air Temperature in Celsius degrees." t:dataTypeName=number

metric m:wet_bulb_temperature l:"Wet Bulb Temperature" d:"Wet bulb temperature in Celsius degrees." t:dataTypeName=number

metric m:humidity l:Humidity d:"Percent relative humidity." t:dataTypeName=number

metric m:rain_intensity l:"Rain Intensity" d:"Rain intensity in mm per hour." t:dataTypeName=number

metric m:interval_rain l:"Interval Rain" d:"Rain since the last hourly measurement, in mm." t:dataTypeName=number

metric m:total_rain l:"Total Rain" d:"Total rain since midnight in mm." t:dataTypeName=number

metric m:precipitation_type l:"Precipitation Type" d:"0 = No precipitation 60 = Liquid precipitation, e.g. rain - Ice, hail and sleet are transmitted as rain (60). 70 = Solid precipitation, e.g. snow 40 = unspecified precipitation" t:dataTypeName=number

metric m:wind_direction l:"Wind Direction" d:"Wind direction in degrees." t:dataTypeName=number

metric m:wind_speed l:"Wind Speed" d:"Wind speed in meters per second." t:dataTypeName=number

metric m:maximum_wind_speed l:"Maximum Wind Speed" d:"Maximum wind speed since midnight in meters per second." t:dataTypeName=number

metric m:barometric_pressure l:"Barometric Pressure" d:"Barometric pressure in hPa." t:dataTypeName=number

metric m:solar_radiation l:"Solar Radiation" d:"Solar radiation in watts per square meter." t:dataTypeName=number

metric m:heading l:Heading d:"The current heading of the wind-measurement unit. The ideal value to get the most accurate measurements is true north (0 degrees) and the unit is manually adjusted, as necessary, to keep it close to this heading" t:dataTypeName=number

metric m:battery_life l:"Battery Life" d:"Battery voltage, an indicator of remaining battery life used by the Chicago Park District to know when batteries should be replaced." t:dataTypeName=number

entity e:7edu-s3u7 l:"Beach Weather Stations - Automated Sensors - 2016 - Humidity" t:attribution="Chicago Park District" t:url=https://data.cityofchicago.org/api/views/7edu-s3u7

property e:7edu-s3u7 t:meta.view d:2017-02-22T15:19:25.097Z v:id=7edu-s3u7 v:category="Parks & Recreation" v:attributionLink=http://www.chicagoparkdistrict.com v:averageRating=0 v:name="Beach Weather Stations - Automated Sensors - 2016 - Humidity" v:attribution="Chicago Park District"

property e:7edu-s3u7 t:meta.view.owner d:2017-02-22T15:19:25.097Z v:id=vewm-vupz v:screenName="Jonathan Levy" v:roleName=administrator v:displayName="Jonathan Levy"

property e:7edu-s3u7 t:meta.view.tableauthor d:2017-02-22T15:19:25.097Z v:id=vewm-vupz v:screenName="Jonathan Levy" v:roleName=administrator v:displayName="Jonathan Levy"

```
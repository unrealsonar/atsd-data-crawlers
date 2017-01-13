# Docker image to start [Energinet Grabber](https://github.com/unrealwork/engrginet-grabber)
Container that collect data from energinet.dk
 
## Run params
 
 You can specify any param that contains app.properties file
 
 ```properties
 atsd.protocol=http
 atsd.host=localhost
 atsd.port=8088
 atsd.user=axibase
 atsd.password=axibase
 interval.hour=24
 download.directory=data
 conf.metrics=conf/metrics.json
 phantom.exec=phatntomjs
 default.entity=energinet.dk
 ```
 
## Example 
 
```sh
docker run -d --name energinet container_image_id \
    atsd.host=atsd_host \
    atsd.port=9088 \
    atsd.user=user \
    atsd.password=password
```
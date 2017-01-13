# Energinet Grabber
Application that collect statistic from http://energinet.dk/

## Build from source
```sh
mvn clean package
```

## Run application 
Download binary archive from releases page

```sh
wget https://github.com/axibase/atsd-data-crawlers/releases/download/0.0.2/energinet-grabber-0.0.2-bin.tar.gz
```
#Extract and run
```sh
tar -xfz energinet-grabber-0.0.2-bin.tar.gz
./energinet-grabber-0.0.2/energinet.sh
```

## Application properties

You can specify following properties in `conf/app.properties` file

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
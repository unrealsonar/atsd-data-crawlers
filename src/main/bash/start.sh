#!/bin/sh

set -e

cd /opt/socrata-crawler/atsd-data-crawlers/
mvn exec:java
cp reports/README.md ../open-data-catalog/
cp reports/series-commands.md ../open-data-catalog/
cp -p reports/datasets/* ../open-data-catalog/datasets
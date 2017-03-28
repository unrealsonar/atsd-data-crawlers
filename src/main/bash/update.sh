#!/bin/sh

set -e

cd /opt/socrata-crawler/atsd-data-crawlres
git pull
mvn clean install
cp src/main/bash/* ../
chmod 755 ../start.sh
chmod 755 ../update.sh
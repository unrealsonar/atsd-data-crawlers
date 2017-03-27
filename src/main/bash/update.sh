#!/bin/sh

set -e

cd /opt/socrata-crawler/atsd-data-crawlres
git pull
mvn clean install
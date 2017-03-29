#!/bin/sh

set -e

cd /opt/socrata-crawler/atsd-data-crawlers/
mvn exec:java
cp reports/README.md ../open-data-catalog/
cp reports/series-commands.md ../open-data-catalog/
cp -p reports/datasets/* ../open-data-catalog/datasets
cd ../open-data-catalog/
. /opt/socrata-crawler/atsd-data-crawlers/src/main/resources/data.properties
git config user.name $github_username
git config user.email $github_email
git remote set-url origin https://$github_username:$github_password@github.com/axibase/open-data-catalog
git add --all
git commit -m "Updated dataset files"
git push origin docker-test
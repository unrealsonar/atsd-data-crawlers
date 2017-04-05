# IRS statistics data crawler

Crawler that collect data from https://www.irs.gov/uac/2017-and-prior-year-filing-season-statistics and convert it to ATSD series commands

## Usage

Get source code

```sh
git clone https://github.com/axibase/atsd-data-crawlers
cd atsd-data-crawlers/
git checkout irs-crawler
```

Build using Maven

```sh
mvn clean install
```

For crawling whole statistics use

```sh
mvn exec:java
```

For crawling statistics from specitic date use "-d" flag (-d yyyy-MM-dd)

```sh
mvn exec:java -Dexec.args="-d 2016-01-01"
```

Result is "series.txt" file which contain series commands

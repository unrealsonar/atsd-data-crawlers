# data-gov-field-checker 

Parser collects some information from datasets from catalog.data.gov via ATSD Collector.

* Logs in ATSD Collector instance (URL of log-in page must be set in data.properties in "collector" property, username must be in "username" property, password must be in "password" property)
* For each JSON dataset URL (which must be set in data.properties in "url" property, separated by semicolons) does the following:
  - creates new Socrata job with URLWizard (on SocrataJob instance with the name set in data.properties in "jobName" property
  - tries to find for current URL its homepage in catalog.data.gov from URLs set in url.properties
  - extracts the following information: all fields from "Dataset", description, 6 first columns from "Columns", "Time", "Series", 3 first series commands from "Commands" and all non-series commands from "Commands"
  - saves all of this to the file named {entity}.md


# atsd-data-crawlers
A collection of various data crawlers for publicly available online resources: html forms, csv files, web services
## List of crawlers
* [Energinet Grabber](https://github.com/axibase/atsd-data-crawlers/tree/energinet-grabber) [[docker image](https://github.com/axibase/atsd-data-crawlers/tree/energinet-grabber-docker)]

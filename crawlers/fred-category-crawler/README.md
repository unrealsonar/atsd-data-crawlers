# FRED Category Crawler

Application that collect categories from https://research.stlouisfed.org using the [API](https://research.stlouisfed.org/docs/api/fred/)

[Source code](https://github.com/axibase/atsd-data-crawlers/tree/fred-category-crawler)

### Command Line Arguments

Name | Required | Description
--- | :---: | :---
-api-key | yes | API key
-ids | yes | List of root category IDs
-dir | no | Output directory for generated files

### Category CSV files:

* [All Categories](resources/all_categories.csv)
* [Nested categories for category #1](resources/nested_for_category_1.csv)
* [Nested categories for category #10](resources/nested_for_category_10.csv)
* [Nested categories for category #3008](resources/nested_for_category_3008.csv)
* [Nested categories for category #32263](resources/nested_for_category_32263.csv)
* [Nested categories for category #32455](resources/nested_for_category_32455.csv)
* [Nested categories for category #32991](resources/nested_for_category_32991.csv)
* [Nested categories for category #31992](resources/nested_for_category_32992.csv)
* [Nested categories for category #33060](resources/nested_for_category_33060.csv)
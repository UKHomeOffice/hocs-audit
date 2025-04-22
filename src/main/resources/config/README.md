# Configuration Files

To support independent releasable services, this folder contains static configuration as to not have to call other
services for the data.

### Case Data Fields

This holds a collection of field names split by case type that should be added to `CASE_DATA` extracts.

#### Schema

```json
{
  <<CASE_TYPE>>: [
    <<FIELD_NAMES>>
  ]
}
```

### Custom Export Views

This holds data surrounding custom views, including:

- The required Keycloak role
- The display name that is prefixed to the filename
- The fields and associated adapters and/or filters for each field

#### Schema

```json
{
  <<VIEW_NAME>>: {
    "displayName": <<REPORT_PREFIX>>,
    "viewName": <<VIEW_NAME>>
    "requiredPermission": <<REQUIRED_ROLE>>,
    "fields": [
      {
        "name": <<FIELD_NAME>>,
        "adapter": <<ADAPTER_NAME>>,
        "filter": {
          "type": "DateRange" | "Value",
          "nullable": boolean, // optional, default false
          "values": string[] // required if type is Value
        }
      }
    ]
  }
}
```

Adapters are optional within the field object. A list of available adapters are
located [here](../../java/uk/gov/digital/ho/hocs/audit/service/domain/adapter).

Filters are controlled by the user via query parameters.
* If the column being filtered has a DateRange filter then the user can pass in dateFrom and/or dateTo
* If the column being filtered has a Value filter then the user can pass in value, which must match one of the values
  defined in the filter's values array
* If the column being filtered has the nullable flag set, then the user can provide includeEmpty to include null values
  for that column, as well as, or instead of the usual filter parameters for that column.

The available parameters are [defined on the endpoint](
../../java/uk/gov/digital/ho/hocs/audit/entrypoint/CustomExportResource.java#L37-L42). Converting those into a 
where clause to filter the view is handled by [CustomExportFilter.java](
../../java/uk/gov/digital/ho/hocs/audit/entrypoint/dto/CustomExportFilter.java).

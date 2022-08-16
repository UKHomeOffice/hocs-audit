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
- The fields and associated adapters for each field

#### Schema

```json
{
  <<VIEW_NAME>>: {
    "displayName": <<REPORT_PREFIX>>
    "requiredPermission": <<REQUIRED_ROLE>>,
    "fields": [
      {
        "name": <<FIELD_NAME>>,
        "adapter": <<ADAPTER_NAME>>
      }
    ]
  }
}
```

Adapters are optional within the field object. A list of available adapters are located [here](../../java/uk/gov/digital/ho/hocs/audit/service/domain/adapter).

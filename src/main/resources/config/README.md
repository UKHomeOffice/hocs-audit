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

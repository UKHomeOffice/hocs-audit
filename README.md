# hocs-audit

This is the Home Office Correspondence Service (HOCS) auditing service. This service is designed to 
receive audit event messages from an SQS queue for persistent storage.

The HOCS project is comprised of a set of micro-services:
* [hocs-workflow](https://github.com/UKHomeOffice/hocs-workflow): models the business processes between the services
* [hocs-frontend](https://github.com/UKHomeOffice/hocs-frontend): the UI service, implemented in Node and React.
* [hocs-casework](https://github.com/UKHomeOffice/hocs-casework): handles the data for each correspondence case.
* [hocs-info-service](https://github.com/UKHomeOffice/hocs-info-service): manages static data and data retrieved through external APIs
* [hocs-docs](https://github.com/UKHomeOffice/hocs-docs): manages processing and storage of documents
* [hocs-audit](https://github.com/UKHomeOffice/hocs-audit): receives and stores audit events.

The source for this service can be found on [GitHub](https://github.com/UKHomeOffice/hocs-audit.git).

<!-- Something about ACP -->

## Getting Started

### Prerequisites

* ```Java 8```
* ```Docker```
* ```Postgres```
* ```SQS```
* ```LocalStack```


## Build and Run the Audit Service

### Preparation
In order to run the service locally, a postgres database, SQS queues, and LocalStack are required. 
These are available through the [docker-compose.yml](docker-compose.yml) file.

To start postgres, sqs, and localstack containers through Docker, from the root of the project run 

```
 docker-compose up 
 ```
In order to stop the containers, run
````$xslt
docker-compose down
````

### Running in an IDE

If you are using an IDE, such as IntelliJ, this audit service can be started by running the ```HocsAuditApplication``` main class. 
The service can then be accessed at ```http://localhost:8088```.

### Building and running without an IDE

This service is built using Gradle. In order to build the project from the command line, run

```
gradle clean build
```
in the root of the project.


<!--- building container locally with gradle clean build and running --->

Alternatively, the corresponding Docker image for this service is available at [quay.io](https://quay.io/repository/ukhomeofficedigital/hocs-audit).

### Flyway and database management

When changes are made to the postgres database through the service they are tracked with Flyway. Any changes which are not tracked will require the database to be restarted. 
To restart the database, from the root of the project run

```$xslt
docker-compose stop postgres
```
and when stopped, restart it by running
```$xslt
docker-compose start postgres
```
and there will be a new instance of postgres.

## Tests

<!--- describe tests here --->

The suite of tests includes unit tests for the resource and services classes, and integration tests. In order to run the integration tests, an instance of postgres must be running.


## Deployment

 See the [pipeline](.drone.yml) for the steps involved in the build and deployment.

## Running the HOCS project

The entire set of services can be run in Docker containers from the
 [hocs-frontend](https://github.com/UKHomeOffice/hocs-frontend) project. Navigate to ```/docker``` from the frontend directory, then run
 
 ```$xslt
./scripts/infrastructure.sh
```
to initiate the infrastructure service containers. These include the postgres, SQS, and LocalStack images. 
When the containers are set up and the services have completed starting, then run

```$xslt
./scripts/services.sh
```
to launch the set of HOCS micro-services.

To stop and clear the service containers run
```
./scripts/clean.sh
```
## Using the Service

<!--- Describe the format of an audit event here. --->

An example audit event message looks like:

```JSON
{
   "correlation_id": "CorrelationID",
    "raising_service": "info-service",
    "audit_payload": "{\"code\":3,\"type\":\"AES\"}",
    "namespace": "Namespace",
    "type": "EVENT_TYPE",
    "user_id": "UserID"
}
```

### Versioning

For versioning this project uses [SemVer](https://semver.org/).

### Authors

This project is authored by the Home Office.

### License 

This project is licensed under the MIT license. For details please see [License](LICENSE) 

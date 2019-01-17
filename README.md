# hocs-audit

This is the Home Office Correspondence Service (HOCS) auditing service. This service is designed to 
receive audit event messages from an sqs queue for persistent storage.

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
<!--- Postgres/SQS/ --->


##Building and running locally

In order to run the service locally, a postgres database and and sqs queue are required. 
These are available through the [docker-compose.yml](docker-compose.yml) at the root of the project.

<!--- Do you need to include sqs queues here?. --->

To start the database instance through Docker, execute 

```
 docker-compose up 
 ```
<!--- need to talk about bringing down the db --->

If you are using an IDE, such as IntelliJ, the service can be run by running the ```HocsAuditApplication``` main class. 
It can then be accessed at ```http://localhost:8088```.



Alternatively, the corresponding Docker image for this service is available at [quay.io](https://quay.io/repository/ukhomeofficedigital/hocs-audit).

The entire set of services can be run in Docker containers from the
 [hocs-frontend](https://github.com/UKHomeOffice/hocs-frontend) project. Navigate to ```/docker``` from the frontend directory, then run
 
 ```$xslt
./scripts/infrastructure.sh
```
to initiate the infrastructure service containers. These include the postgres, AWS command line interface, and the localstack images. 
When the containers are set up and the services have completed starting, then run

```$xslt
./scripts/services.sh
```
to launch the HOCS micro-services.


<!--- building container locally with gradle clean build and docker build -t hocs-audit-local . --->

<!--- Flyway --->  

##Tests

In order to run the integration tests, an instance of Postgres must be running before starting the tests.

<!--- describe tests here --->

## Deployment

Deployment is managed with Drone, using Gradle to build. On evey push to master, Drone will:
* checkout and build the project using Gradle
* create the Docker image for the service 
* push the Docker image to Quay.io
* deployed with kubernetes ???

 See the [pipeline](.drone.yml) for the steps involved in the build and deployment.


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

###Contributing

###Versioning


### Authors

### License 

This project is licensed under the MIT license. For details please see [License](LICENSE) 
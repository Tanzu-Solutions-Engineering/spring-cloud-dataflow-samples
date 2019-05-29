# Spring Batch Demo for Spring Cloud Data Flow

The projects here contain Spring Batch based applications used to demo [Composed Task](https://dataflow.spring.io/docs/batch-developer-guides/batch/data-flow-composed-task/)

## Requirements
- wget
- Java 1.8+ 
- Maven
- Database tool to query MySQL to verify results.(Make sure to expose MySQL port locally if using docker)
- Python 3+ (Only to run local webserver to make it easier for SCDF to load our apps)

## Quickstart

Install Docker for your desktop (engine version 18.09.2 or higher)

Download docker-compose file
```
wget https://raw.githubusercontent.com/spring-cloud/spring-cloud-dataflow/2.1.0.RELEASE/spring-cloud-dataflow-server/docker-compose.yml
```

Expose MySQL port so we can use a DB visiblewer to verify results.
- Open the docker-compose.yml file that we downloaded and add the following lines to the `mysql` section

```
ports:
        - "33061:3306"
```

Start it up!
```
DATAFLOW_VERSION=2.1.0.RELEASE SKIPPER_VERSION=2.0.2.RELEASE docker-compose up

```

Connect your favorite MySQL viewer to port 33061 on localhost.

## Modules

### core
Contains common code used by the rest of the applications

### file-ingest

Spring batch application that reads first name and last name from a given csv file as `filepath` parameter and write to the database table called `persons`. The default behavior of the common [PersonItemProcessor](batch/core/src/main/java/io/spring/cloud/dataflow/batch/processor/PersonItemProcessor.java) is to not do anything to the payload

### db-transform

Spring batch application that reads first name and last name from `persons` table and apply an optional transformation via the [PersonItemProcessor](batch/core/src/main/java/io/spring/cloud/dataflow/batch/processor/PersonItemProcessor.java). The options supported are "NONE","UPPERCASE","LOWERCASE","BACKWARDS"


## Build

Import each of the modules into your IDE or

```
$mvn package
```
for each module. `core` needs to be built first!

## How to Run
The following instructions assume you are running it locally using docker. To make it easy to register the application with SCDF locally, copy the file-ingest and db-transform jar files to a common directory and run a web server in the directory so SCDF.

From the batch directoy run

```
mkdir tasks
cp file-ingest/target/ingest-1.0.0.BUILD-SNAPSHOT.jar ./tasks
cp cp db-transform/target/dbtransform-1.0.0.BUILD-SNAPSHOT.jar ./tasks
cd tasks
python -m SimpleHTTPServer 8000
```

### SCDF Shell
We will use the SCDF shell to install our applications. To run:

```
docker exec -it dataflow-server java -jar shell.jar
```

### Simple File Ingest

#### Register the file-ingest app
app register --name Demo-ImportFileApp --type task --uri http://host.docker.internal:8000/ingest-1.0.0.BUILD-SNAPSHOT.jar

#### Verify
app info --name Demo-ImportFileApp --type task

#### Create task
task create ImportTask --definition "ImportFile: Demo-ImportFileApp --filepath=classpath:data.csv"

#### Run the task
task launch ImportTask --arguments "--increment-instance-enabled=true"

### Database Transform

#### Register the db-transform app
app register --name Demo-DbTransformApp --type task --uri http://host.docker.internal:8000/db-transform-1.0.0.BUILD-SNAPSHOT.jar

#### Verify
app info --name Demo-DbTransformApp --type task

#### Create task
task create DbUppercaseTask --definition "Uppercase: Demo-DbTransformApp --action=UPPERCASE"

#### Run the task
task launch DbUppercaseTask --arguments "--increment-instance-enabled=true"

### Composed Task that demos Distributed Saga Pattern

#### Create the Composed Task

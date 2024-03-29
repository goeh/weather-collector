# Weather Data Collector

## A data collector written in Java that collects data from weather stations.

This project was developed by me when I wanted to collect data from my weather station (Davis Vantage Pro2) and I did
not want to use a Windows server for the job. I have an Ubuntu LAMP server in the closet, and it has been running this
data collector since 2008, collecting weather data every 10 minutes without interruption.

Since 2017 a Raspberry Pi 3 has taken over the task of downloading weather data at my home. No changes to the program
needed.

I've also successfully tested the system on a Raspberry Pi 4 with Raspberry Pi OS Lite (Release date: May 7th 2021).

See https://github.com/goeh/weather-visualizer for how data can be visualized.

## Prerequisites

### git

To download and build from source, git must be installed on the Raspberry Pi. To install git, use the following command:

    sudo apt install git

### Java

Java must be installed on the Raspberry Pi prior to building and running the programs.
To install the default JDK, use the following command:

    sudo apt install default-jdk

Or to install a specific version:

    sudo apt-get install openjdk-11-jdk

Suggest taking a look at [sdkman](https://sdkman.io/) to easily manage different Java and/or Gradle versions!

## Build

    ./gradlew

When the build finish successfully, you will find `weather-collector.zip` in ./build/distributions. Extract the archive
where you want to install the program, for example in /home/pi/weather. Then go to the folder where you extracted the
archive and start the weather collector.

## Run

To run the program you must have a configuration file. See section "Sample collector.properties" below.

For a quick test, create a file called `collector.properties`with this content:

datastore.type=dummy datastore.dummy.class=se.technipelago.weather.datastore.DummyDataStore

### Run from distribution

Unzip the archive located in ./build/distributions/weather-collector-VERSION.zip to a folder on the server that your
Davis weather station is connected to.

    cd weather-collector-VERSION
    ./bin/weather-collector /dev/ttyUSB0 19200

With `VERSION` being equal to the release version e.g. `1.5.0`.

### Run from source

    ./gradlew run --args='/dev/ttyUSB0 19200'

If you prefix the command with `--test`, the program will print info to the console and then stop.

    ./gradlew run --args='--test /dev/ttyUSB0 19200'

    The station is a Vantage Pro or Vantage Pro 2
    Console Time: 2021-10-09T23:02:15.000+0200

### Weather data storage

Weather data downloaded from the weather station is stored by a data storage backend. There are a few data storage
backend available in core:

* DummyDataStore - Prints data to console / stdout
* SqlDataStore - Stores data in a SQL / JDBC database
* RemoteDataStore - POST data to an external HTTP endpoint

The default database engine used by the SqlDataStore is H2 (www.h2database.com) and data is stored in a file
called `weather-db.mv.db`. You can configure another JDBC database using the `datastore.xxx` properties (see below). A
suitable JDBC driver .jar must be located in the weather-collector-VERSION/lib folder at runtime.

The first time you run the program with an empty database, it downloads **all** records from the weather station. This
can take a long time (10 minutes or more). The next time you run the program, it will only download records created
since the last download. That should only take a minute.

### Sample collector.properties

Put `collector.properties` in the `weather-collector-VERSION` directory and `cd` to that directory before you start the
program. You can also put `collector.properties` anywhere on the Java classpath.

#### MySQL storage

    datastore.type=jdbc
    datastore.jdbc.class=se.technipelago.weather.datastore.sql.SqlDataStore
    datastore.jdbc.name=weather
    datastore.jdbc.driver=com.mysql.cj.jdbc.Driver
    datastore.jdbc.url=jdbc:mysql://localhost:3306/weather?user=weather&password=weather

Install MySQL/MariaDB on you Raspberry Pi.

    sudo apt install mariadb-server

Set root password and secure your MariaDB installation
(it's a good idea to answer Y (yes) to all questions).

    sudo mysql_secure_installation

Create a `weather` database.

    sudo bash
    mysql -u root -p
    (enter root password you assigned above)
    
    (at SQL prompt)
    MariaDB [(none)]> CREATE USER weather IDENTIFIED BY 'weather';
    MariaDB [(none)]> CREATE DATABASE weather;
    MariaDB [(none)]> GRANT ALL ON weather.* TO weather;
    MariaDB [(none)]> exit

    exit # to leave 'root' user and go back to 'pi'

To verify that weather records are stored in the database after you run the download command.

    mysql -u weather -p weather -e 'SELECT ts, temp_in, hum_in, barometer FROM archive ORDER BY ts'

#### No storage (for testing)

The dummy data store does not store anything. It just prints weather data to the console / stdout.

    datastore.type=dummy
    datastore.dummy.class=se.technipelago.weather.datastore.DummyDataStore

#### Multiple data stores

To store data in more than one data store, set `datastore.type` to a comma separated string and configure each data
store with `datastore.xxx` prefix.

    datastore.type=db,ds1,ds2

    datastore.db.class=se.technipelago.weather.datastore.sql.SqlDataStore
    datastore.db.driver=com.mysql.cj.jdbc.Driver
    datastore.db.url=jdbc:mysql://localhost:3306/weather?user=weather&password=weather

    datastore.ds1.class=se.technipelago.weather.datastore.DummyDataStore

    datastore.ds2.class=se.technipelago.weather.datastore.remote.RemoteDataStore
    datastore.ds2.url=https://api.some.domain/weather/upload
    datastore.ds2.client.key=some-value
    datastore.ds2.client.secret=some-secret-value

To make it easier to start the data collector from `cron` or from the command line, create a start script.

### Start script for Raspberry Pi (Raspbian)

    #!/bin/bash
    #
    WEATHER_HOME=$HOME/weather
    VERSION=1.5.0       # Example
    COLLECTOR_HOME=$WEATHER_HOME/weather-collector-$VERSION
    SERIAL_PORT=/dev/ttyUSB0
    SERIAL_BAUD=19200
    
    cd $COLLECTOR_HOME
    bin/weather-collector $SERIAL_PORT $SERIAL_BAUD

### Logging

Log4j2 is used for logging. See [logging.apache.org](https://logging.apache.org) for configuration options.

Full debug logging to the console can be great the first time you run the program, to see that things works ok. Later
you can configure a file logger with reduced logging in production.

Log to console (debug logging)

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration status="WARN">
        <appenders>
            <console name="Console" target="SYSTEM_OUT">
                <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
            </console>
        </appenders>
        <loggers>
            <logger name="se.technipelago" level="DEBUG" additivity="false">
                <AppenderRef ref="Console"/>
            </logger>
            <root level="ERROR">
                <AppenderRef ref="Console"/>
            </root>
        </loggers>
    </configuration>

Log to file (production logging)

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration status="WARN">
        <appenders>
            <appender name="FILE" class="org.apache.log4j.FileAppender">
            
               <param name="file" value="/var/log/weather-collector.log"/>
               <param name="immediateFlush" value="true"/>
               <param name="threshold" value="debug"/>
               <param name="append" value="false"/>
            
               <layout class="org.apache.log4j.PatternLayout">
                  <param name="conversionPattern" value="%m%n"/>
               </layout>
            </appender>
        </appenders>
    
        <loggers>   
            <logger name="log4j.rootLogger" additivity="false">
               <level value="INFO"/>
               <appender-ref ref="FILE"/>
            </logger>
        </loggers>
    </configuration>

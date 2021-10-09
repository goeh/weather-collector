# Weather Data Collector

## A data collector written in Java that collects data from weather stations.

This project was developed by me when I wanted to collect data from my
weather station (Davis Vantage Pro2) and I did not want to use a
Windows server for the job. I have an Ubuntu LAMP server in the closet,
and it has been running this data collector since 2008,
collecting weather data every 10 minutes without interruption.

Since 2017 a Raspberry Pi 3 has taken over the task of downloading weather data at my home.
No changes to the program needed.

See https://github.com/goeh/weather-visualizer for how data can be visualized.

## Prerequisites

### git

To download and build from source, git must be installed on the Raspberry Pi.
To install git, use the following command:

    sudo apt-get install git

### Java

Java must be installed on the Raspberry Pi prior to building and running the programs.
To install the default JDK, use the following command:

    sudo apt-get install default-jdk

Or to install a specific version:

    sudo apt-get install openjdk-11-jdk

Suggest taking a look at [sdkman](https://sdkman.io/) to easily manage different Java and/or Gradle versions!

## Build

    ./gradlew

When the build finish successfully, you will find `weather-collector.zip` in ./build/distributions.
Extract the archive where you want to install the program, for example in /home/pi/weather.
Then go to the folder where you extracted the archive and start the weather collector.

## Run

    cd weather-collector-VERSION
    ./bin/weather-collector /dev/ttyUSB0 19200

With `VERSION` being equal to the release version e.g. `1.5.0`.

The default database engine is H2 (www.h2database.com) and data is stored in a file called `weather-db.mv.db`. You can
configure another JDBC database using the `datastore.xxx` properties (see below). A suitable JDBC driver .jar must be
located in the weather-collector-VERSION/lib folder at runtime.

The first time you run the program with an empty database, it downloads **all** records from the weather station. This
can take a long time (10 minutes). The next time you run the program, it will only download records created since the
last download, so that should only take a few seconds.

To make it easier to start the collector from `cron` or from the command line, create a start script.

### Start script for Raspberry Pi (Raspbian)

    #!/bin/bash
    #
    WEATHER_HOME=$HOME/weather
    VERSION=1.5.0       # Example
    COLLECTOR_HOME=$WEATHER_HOME/weather-collector-$VERSION
    SERIAL_PORT=/dev/ttyUSB0
    SERIAL_BAUD=19200
    
    export WEATHER_COLLECTOR_OPTS="-Djava.util.logging.config.file=$COLLECTOR_HOME/collector-logging.properties"
    cd $COLLECTOR_HOME
    bin/weather-collector $SERIAL_PORT $SERIAL_BAUD

### Sample collector.properties

Put `collector.properties` in the `weather-collector-VERSION` directory and `cd` to that directory before you start the
program. You can also put `collector.properties` anywhere on the Java classpath.

#### MySQL storage

    datastore.type=jdbc
    datastore.jdbc.class=se.technipelago.weather.datastore.sql.SqlDataStore
    datastore.name=weather
    datastore.jdbc.driver=com.mysql.jdbc.Driver
    datastore.jdbc.url=jdbc:mysql://localhost:3306/weather?user=weather&password=weather

#### No storage (for testing)

    datastore.type=dummy
    datastore.dummy.class=se.technipelago.weather.datastore.DummyDataStore

### Sample collector-logging.properties

Full debug logging to the console can be great the first time you run the program, to see that things works ok. Later
you can configure a file logger with reduced logging in production.

Put `collector-logging.properties` in the `weather-collector-VERSION` directory.

Log to console (debug logging)

    handlers = java.util.logging.ConsoleHandler

    java.util.logging.ConsoleHandler.level = INFO
    java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
    java.util.logging.SimpleFormatter.format=%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s %4$s: %5$s%n

    se.technipelago.level = ALL

Log to file (production logging)

    handlers = java.util.logging.FileHandler
    
    java.util.logging.FileHandler.level = INFO
    java.util.logging.FileHandler.pattern = collector.log
    java.util.logging.FileHandler.limit = 100000
    java.util.logging.FileHandler.count = 5
    java.util.logging.FileHandler.append= true
    java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
    java.util.logging.SimpleFormatter.format=%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s %4$s: %5$s%n

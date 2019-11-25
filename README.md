# Weather Data Collector

## A data collector written in Java that collects data from weather stations.

This project was developed by me when I wanted to collect data from my
weather station (Davis Vantage Pro2) and I did not want to use a
Windows server for the job. I have a Ubuntu LAMP server in the closet
and it has been running this data collector since 2008,
collecting weather data every 10 minutes without interruption.

Since 2017 a Raspberry Pi 3 has taken over the task of downloading weather data at my home.
No changes to the program needed.

## Build

    ./gradlew

## Start script for Raspberry Pi (Raspbian)

    #!/bin/bash
    #
    WEATHER_HOME=$HOME
    COLLECTOR_HOME=$WEATHER_HOME/weather-collector-1.4.1
    SERIAL_PORT=/dev/ttyUSB0
    SERIAL_BAUD=9600
    
    export COLLECTOR_OPTS="-Djava.util.logging.config.file=$COLLECTOR_HOME/collector-logging.properties"
    cd $COLLECTOR_HOME
    bin/weather-collector $SERIAL_PORT $SERIAL_BAUD

## Sample collector.properties

Put `collector.properties` in the `weather-collector-1.4.1` directory and `cd` to that directory before you start the program.

    datastore.type=jdbc
    datastore.name=weather
    datastore.jdbc.driver=com.mysql.jdbc.Driver
    datastore.jdbc.url=jdbc:mysql://localhost:3306/weather?user=weather&password=weather

## Sample collector-logging.properties

Put `collector-logging.properties` in the `weather-collector-1.4.1` directory.

    handlers = java.util.logging.FileHandler
    
    java.util.logging.FileHandler.level = ALL
    java.util.logging.FileHandler.pattern = collector.log
    java.util.logging.FileHandler.limit = 100000
    java.util.logging.FileHandler.count = 5
    java.util.logging.FileHandler.append= true
    java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
    java.util.logging.SimpleFormatter.format=%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s %4$s: %5$s%n
    
    se.technipelago.level = INFO

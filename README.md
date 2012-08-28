# Weather Data Collector
## A data collector written in Java that collects data from weather stations.

This project was developed by me when I wanted to collect data from my
weather station (Davis Vantage Pro2) and I did not want to use a
Windows server for the job. I have a Ubuntu LAMP server in the closet
and it has been running this data collector since 2008,
collecting weather data every 10 minutes without interruption.

### start script

    #!/bin/bash
    #
    COLLECTOR_HOME=$HOME
    SERIAL_HOST=localhost
    SERIAL_PORT=8888
    
    java -classpath $COLLECTOR_HOME/collector-1.1.jar:$COLLECTOR_HOME/mysql-connector-java-5.1.11-bin.jar se.technipelago.weather.vantagepro.DownloadController $SERIAL_HOST $SERIAL_PORT

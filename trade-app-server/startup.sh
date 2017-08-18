#!/bin/bash

cd /var/log/trade-app

# Start Trade App Server
/usr/bin/java -Dspring.profiles.active=prod -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9990 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar /app/trade-app-0.1.0.jar &

# Wait for server
sleep 15

# Start injector service
/usr/bin/java -cp /app/trade-app-0.1.0.jar -Dspring.profiles.active=prod -Dloader.main=com.neueda.trade.injector.Injector org.springframework.boot.loader.PropertiesLauncher

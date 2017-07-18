REM This script should be in the same folder as the trade-app JAR file

java -cp trade-app-0.1.0.jar -Dspring.profiles.active=prod -Dloader.main=com.neueda.trade.injector.Injector org.springframework.boot.loader.PropertiesLauncher

FROM ubuntu:17.10
MAINTAINER Neueda and TPS
LABEL Version="1.0"
LABEL Description="Docker container to run Martins trade app server"
EXPOSE 8080
EXPOSE 9990
RUN apt-get -y update
RUN apt-get -y install openjdk-8-jdk
RUN mkdir /app /var/log/trade-app
COPY trade-app-0.1.0.jar /app/
COPY startup.sh /app/
RUN chmod +x /app/startup.sh
ENTRYPOINT ["/app/startup.sh"]

FROM centos:latest
MAINTAINER Neueda and TPS
LABEL Version="1.0"
LABEL Description="Docker container to run Martins trade app"
EXPOSE 8080
RUN yum -y install java-1.8.0-openjdk
RUN mkdir /app
COPY target/trade-app-0.0.1-SNAPSHOT.jar /app/
ENTRYPOINT ["/usr/bin/java","-jar","/app/trade-app-0.0.1-SNAPSHOT.jar"]

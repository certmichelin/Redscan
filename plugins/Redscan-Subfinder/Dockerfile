FROM golang:1.22 AS subfinder

#Build subfinder
RUN apt-get update && apt-get install -y git
RUN git clone https://github.com/projectdiscovery/subfinder.git /go/src/github.com/projectdiscovery/subfinder
WORKDIR /go/src/github.com/projectdiscovery/subfinder
RUN git checkout tags/v2.6.6
WORKDIR /go/src/github.com/projectdiscovery/subfinder/v2
RUN go mod download
RUN go build ./cmd/subfinder

FROM openjdk:23-jdk
#################################################################
# Install scanner requirement here. (nmap example here)

#Copy subfinder
COPY --from=subfinder /go/src/github.com/projectdiscovery/subfinder/v2/subfinder /usr/local/bin/subfinder
RUN chmod +x /usr/local/bin/subfinder



#Create config directory and config file
RUN mkdir /root/.config/
RUN mkdir /root/.config/subfinder/
RUN touch /root/.config/subfinder/config.yaml
#################################################################

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

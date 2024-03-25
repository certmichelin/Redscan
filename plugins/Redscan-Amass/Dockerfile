FROM golang:1.19 AS amass

#Build amass
RUN apt-get update && apt-get install -y git
RUN git clone https://github.com/OWASP/Amass.git /go/src/github.com/OWASP/Amass
WORKDIR /go/src/github.com/OWASP/Amass
RUN git checkout tags/v3.23.3
RUN go install ./...

FROM openjdk:8-jre

#Copy amass
COPY --from=amass /go/bin/amass /usr/local/bin/amass
RUN chmod +x /usr/local/bin/amass

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

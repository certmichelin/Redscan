FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install gospider
RUN apk add go
ENV PATH="/root/go/bin:${PATH}"
RUN go install github.com/jaeles-project/gospider@latest
#################################################################

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

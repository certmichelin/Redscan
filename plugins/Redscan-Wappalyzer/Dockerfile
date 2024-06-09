FROM deddobifu/redscan-base:1.0.0

#################################################################
#Install Node JS
RUN apk add --no-cache nodejs npm chromium

WORKDIR /usr/local/lib
RUN npm i -g wappalyzer@6.10.66
#################################################################

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]
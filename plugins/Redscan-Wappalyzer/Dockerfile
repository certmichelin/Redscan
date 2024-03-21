FROM openjdk:8-jre

#################################################################
RUN curl -sL https://deb.nodesource.com/setup_18.x | bash -
RUN apt update -y
RUN apt upgrade -y
RUN apt install nodejs chromium -y

WORKDIR /usr/local/lib
RUN npm i -g wappalyzer@6.10.55
#################################################################

WORKDIR /
#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

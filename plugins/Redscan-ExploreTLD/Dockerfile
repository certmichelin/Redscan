FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)

#Install dig on Alpine
RUN apt update
RUN apt install dnsutils -y

#Copy the default tld list.
COPY ./tld_list.dat /wordlists/tld_list.dat
#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

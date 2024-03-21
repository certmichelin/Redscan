FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)

#RUN apt update
#RUN apt install -y alien
#RUN wget https://nmap.org/dist/nmap-7.91-1.x86_64.rpm  -O /tmp/nmap-7.91-1.x86_64.rpm
#RUN alien -i /tmp/nmap-7.91-1.x86_64.rpm
#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

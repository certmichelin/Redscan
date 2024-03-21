FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)
RUN apt update
RUN apt install zip -y
RUN wget https://github.com/OWASP/Amass/releases/download/v3.23.2/amass_Linux_amd64.zip
RUN unzip ./amass_Linux_amd64.zip
RUN chmod +x ./amass_Linux_amd64/amass
RUN mv ./amass_Linux_amd64/amass /usr/bin
#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

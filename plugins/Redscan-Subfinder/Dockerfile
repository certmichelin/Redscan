FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)

#Install subfinder
RUN wget https://github.com/projectdiscovery/subfinder/releases/download/v2.5.5/subfinder_2.5.5_linux_amd64.zip
RUN unzip subfinder_2.5.5_linux_amd64.zip
RUN chmod +x ./subfinder
RUN mv ./subfinder /usr/bin

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

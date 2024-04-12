FROM deddobifu/redscan-base:1.0.2

#################################################################
# Install scanner requirement here. (nmap example here)

#Install subfinder
RUN go install -v github.com/projectdiscovery/subfinder/v2/cmd/subfinder@v2.6.6

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

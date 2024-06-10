FROM deddobifu/redscan-base-17:1.0.0

#################################################################
# Install scanner requirement here. (nmap example here)
#################################################################

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

# Expose port
EXPOSE 8888

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

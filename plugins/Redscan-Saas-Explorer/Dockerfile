FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)

#################################################################

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

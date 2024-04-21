FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install scanner requirement here. (nmap example here)
RUN apk add --no-cache git python3 py3-pip

RUN git clone https://github.com/certmichelin/cloud_enum.git /usr/bin/cloud_enum

WORKDIR /usr/bin/cloud_enum
RUN pip3 install -r requirements.txt --break-system-packages
WORKDIR /

#################################################################

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml", "-jar","/app.jar"]

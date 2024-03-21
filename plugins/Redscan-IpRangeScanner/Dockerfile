FROM openjdk:8-jre

#################################################################
#Masscan install.

RUN apt update
RUN apt install -y make gcc libpcap-dev git

RUN mkdir /tmp/masscan_install
WORKDIR /tmp/masscan_install
RUN git clone https://github.com/robertdavidgraham/masscan.git
WORKDIR /tmp/masscan_install/masscan
RUN make install
RUN rm -rf /tmp/masscan_install
WORKDIR /
#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

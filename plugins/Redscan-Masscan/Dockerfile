FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install scanner requirement here. (nmap example here)

#Masscan 1.3.2 install.
RUN apk add make gcc libc-dev linux-headers libpcap-dev git
RUN mkdir /tmp/masscan_install
WORKDIR /tmp/masscan_install
RUN git clone https://github.com/robertdavidgraham/masscan.git
WORKDIR /tmp/masscan_install/masscan
RUN make install
RUN rm -rf /tmp/masscan_install
WORKDIR /

#################################################################

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

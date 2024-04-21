FROM deddobifu/redscan-base:1.0.0

#################################################################
#Masscan install.

RUN apk add --no-cache git gcc make musl-dev libpcap-dev linux-headers

RUN mkdir /tmp/masscan_install
WORKDIR /tmp/masscan_install
RUN git clone https://github.com/robertdavidgraham/masscan.git
WORKDIR /tmp/masscan_install/masscan
RUN make install
RUN rm -rf /tmp/masscan_install
WORKDIR /

#Copy default top 1000 nmap ports.
COPY nmap-ports-top1000.txt /wordlists/nmap-ports-top1000.txt

#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

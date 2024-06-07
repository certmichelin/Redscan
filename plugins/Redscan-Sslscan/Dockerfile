FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install scanner requirement here. 

RUN apk add --no-cache git gcc make perl libc-dev linux-headers zlib-dev openssl
RUN git clone https://github.com/rbsec/sslscan.git
WORKDIR /sslscan
RUN git checkout tags/2.1.3
RUN make static
RUN make install
WORKDIR /
#################################################################

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

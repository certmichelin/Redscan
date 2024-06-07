FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install scanner requirement here. (nmap example here)

#Install Go.
RUN apk add --no-cache go
ENV PATH="/root/go/bin:${PATH}"

#Install subfinder
RUN go install -v github.com/owasp-amass/amass/v4/...@master
#################################################################

#Copy default wordlist
COPY prefixes.txt /wordlists/prefixes.txt

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]
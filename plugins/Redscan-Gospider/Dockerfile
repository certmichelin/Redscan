FROM openjdk:8-jre

#################################################################
# Install gospider
RUN apt update
RUN apt install git -y
RUN wget https://golang.org/dl/go1.20.linux-amd64.tar.gz
RUN tar -C /usr/local -xzf go1.20.linux-amd64.tar.gz
RUN export PATH=$PATH:/usr/local/go/bin
RUN rm /go1.20.linux-amd64.tar.gz
RUN /usr/local/go/bin/go install github.com/jaeles-project/gospider@latest

#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

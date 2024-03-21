FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)

RUN apt update
RUN apt install git -y
RUN wget https://golang.org/dl/go1.20.linux-amd64.tar.gz
RUN tar -C /usr/local -xzf go1.20.linux-amd64.tar.gz
RUN export PATH=$PATH:/usr/local/go/bin
RUN /usr/local/go/bin/go install github.com/certmichelin/subjack@latest

WORKDIR /
RUN rm -rf /subjack
#################################################################

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

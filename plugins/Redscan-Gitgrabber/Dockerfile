FROM openjdk:8-jre

#################################################################
# Install scanner requirement here. (nmap example here)

#Install gitgrabber
RUN apt update
RUN apt install git -y
RUN apt install python3.9 -y
RUN apt install python3-pip -y

RUN git clone https://github.com/hisxo/gitGraber /usr/bin/gitgrabber
WORKDIR /usr/bin/gitgrabber
RUN pip3 install -r requirements.txt
WORKDIR /
#################################################################

#Copy the war.
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]

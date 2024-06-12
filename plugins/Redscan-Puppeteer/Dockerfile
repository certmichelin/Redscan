FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install prerequisite for Puppeteer (Chromium, Node.js, NPM, Yarn)
RUN apk update && apk add --no-cache chromium nss freetype freetype-dev harfbuzz ca-certificates ttf-freefont nodejs npm yarn

# Set environment variables for Puppeteer
# Tell Puppeteer to skip installing Chrome. We'll be using the installed package.
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true \
    PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser

WORKDIR /usr/local/bin
RUN npm install puppeteer

COPY scripts/screenshot_puppeteer.js /usr/local/bin
WORKDIR /
#################################################################

#Setup the default log4j2.conf.
ARG LOG4J2_FILE=src/main/resources/log4j2-spring.xml
COPY ${LOG4J2_FILE} /conf/log4j2.xml

ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} app.jar
CMD ["java", "-Dlogging.config=/conf/log4j2.xml","-jar","/app.jar"]


FROM alpine:3.19.1

#Install common tools.
RUN apk add --no-cache git make gcc curl wget bash procps musl-dev

#Install Go.
RUN apk add --no-cache go
ENV PATH="/root/go/bin:${PATH}"

#Install OpenJDK 8
RUN apk --no-cache add openjdk8-jre
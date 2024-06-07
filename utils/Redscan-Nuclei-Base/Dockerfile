FROM deddobifu/redscan-base:1.0.0

#################################################################
# Install scanner requirement here. (nmap example here)

#Install Go.
RUN apk add --no-cache go
ENV PATH="/root/go/bin:${PATH}"

#Install nuclei 
RUN go install -v github.com/projectdiscovery/nuclei/v3/cmd/nuclei@v3.2.8
RUN nuclei -update-templates

#Install launcher
RUN apk add gcc libc-dev linux-headers
COPY nucleilauncher.c /nucleilauncher.c
RUN gcc /nucleilauncher.c -o /nucleilauncher
#################################################################


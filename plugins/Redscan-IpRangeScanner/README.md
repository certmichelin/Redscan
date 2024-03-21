## REDSCAN-IPRANGESCANNER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | IP_RANGE                                     |
| Send to       | IP                                           |
| Tools used    | Plain java + masscan                         |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

The purpose of this worker is to detect online host from IP Range.

### Description

The detection is made in two steps :
1. Ping the IP address.
2. If the ping failed then execute a post scan with masscan to verify is the host is online.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.15.0

# If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```
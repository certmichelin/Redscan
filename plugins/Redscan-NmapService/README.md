## REDSCAN-NMAPSERVICE

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_SERVICES_EXCHANGE_NAME                |
| Send to       |                                              |
| Tools used    | nmap 7.91-1                                  |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Determine nature of exposed services.

### Description

Use NMAP service detection feature to enumerate service exposed. 
The value retrieved is cached for 24 hours.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```

## REDSCAN-REVERSEWHOIS

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME           |
| Send to       | 
| Tools used    | amass                                        |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Search for new masterdomains via amass

### Description

Use amass Intelligence option to query several APIs to gather masterdomains alike.
This will check for @masterdomains.com or masterdomains.com information in whois database. 

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```

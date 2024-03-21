## REDSCAN-CLOUDENUM

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  |   FANOUT_BRANDS_EXCHANGE_NAME                |
| Send to       |   FANOUT_VULNERABILITY_EXCHANGE_NAME         |
| Tools used    | cloud_enum fork from initstring              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Lateralize Recon phase in order to obtain new domains using brand name and check associated potential bucket.

### Description

Use fuzzed string to check if a company got open or protected bucket (aws, google, azure) and eventually new domains. 

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```

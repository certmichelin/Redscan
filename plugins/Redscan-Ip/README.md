## REDSCAN-IP

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME                 |
| Send to       | N/A                                          |
| Tools used    | N/A                                          |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Retrieve the domain ip.

### Description

Resolve the domain name.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```
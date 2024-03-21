## REDSCAN-SUBJACK

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME                 |
| Send to       | FANOUT_VULNERABILITIES_EXCHANGE_NAME         |
| Tools used    | Subjack                                      |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Detect potential subdomain takeover.

### Description

User subjack to determine potential domain takeover.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```
## REDSCAN-MASSCAN

| Attribute     | Value                                                                  |
| ------------- | ---------------------------------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME, FANOUT_IPS_EXCHANGE_NAME                 |
| Send to       | FANOUT_SERVICES_EXCHANGE_NAME                                          |
| Tools used    | masscan 1.3.2                                                          |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file                           |

### Objective

Realize port scan using masscan

### Description

Masscan is used with the nmap top 1000 wordlist.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```

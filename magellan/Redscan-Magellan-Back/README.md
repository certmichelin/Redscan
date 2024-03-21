## REDSCAN-MAGELLAN-BACK

| Attribute     | Value                                                                                         |
| ------------- | --------------------------------------------------------------------------------------------- |
| Subscribe to  | N/A                                                                                           |
| Send to       | FANOUT_BRANDS_EXCHANGE_NAME,FANOUT_IPRANGES_EXCHANGE_NAME, FANOUT_MASTERDOMAINS_EXCHANGE_NAME |
| Tools used    |                                                                                               |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file                                                  |

### Objective

Manage the brands & master domains.

### Description

REST API used to manage the brands.

### How to develop

```
docker run -d -p 15671:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9201:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.15.0
```

## REDSCAN-VULNERABILITY

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_VULNERABILITIES_EXCHANGE_NAME         |
| Send to       | FANOUT_ALERTS_EXCHANGE_NAME                  |
| Tools used    |                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Manage vulnerabilities raised by workers.

### Description

The purpose is to verify if a vulnerability raised by workers was already detected, fixed and so on. According to this triage, the vulnerability can be moved to an alert and sent to alert worker.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.15.0
```
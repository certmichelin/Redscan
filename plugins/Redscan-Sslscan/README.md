## REDSCAN-SSLSCAN

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_HTTP_SERVICE_EXCHANGE_NAME            |
| Send to       | FANOUT_VULNERABILITIES_EXCHANGE_NAME         |
| Tools used    | SSLScan 2.0.10                               |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Inspect SSL/TLS configuration (Weak protocols/ciphers) and check Heartbleed vulnerability on HTTPS Service.

### Description

Use SSl scan ot gather intel on certificate and on SSL/TLS. Send vulnerability accordingly:

- Obsolete protocol. eg: SSL
- Obsolete protocol version. eg: TLS1.0
- Heartbleed

### How to develop

WARNING: For dev you need to change the condition to check if the target is SSL (changed it to if (true))

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```

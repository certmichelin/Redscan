## REDSCAN-WAPPALYZER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_HTTP_SERVICES_EXCHANGE_NAME           |
| Send to       | N/A                                          |
| Tools used    | Wappalyzer                                   |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Determine which technologies an HTTP service uses.

### Description

Use Wappalyzer to enumerate the technologies used. The value retrieved is cached for 24 hours.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```

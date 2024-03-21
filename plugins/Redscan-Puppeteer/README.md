## REDSCAN-PUPPETEER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_EXCHANGE_HTTP_SERVICES                |
| Send to       | N/A                                          |
| Tools used    | Puppeteer/Chromium                           |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Take a screenshot of a web page.

### Description

Use Puppeteer (which uses Chromium) to take screenshots of a web pages.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```

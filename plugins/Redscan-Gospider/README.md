## REDSCAN-GOSPIDER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | new HttpService().getFanoutExchangeName()    |
| Send to       |                                              |
| Tools used    | GoSpider                                     |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Spider http and https services.

### Description

Determine accesible urls using gospider tool.

### How to develop

```
docker login https://ghcr.io
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1

# If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```
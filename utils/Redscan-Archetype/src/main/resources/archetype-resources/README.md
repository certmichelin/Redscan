#set($hash = '#')
${hash}${hash} ${artifactId.toUpperCase()}

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  |                                              |
| Send to       |                                              |
| Tools used    |                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

${hash}${hash}${hash} Objective

${hash}${hash}${hash} Description

${hash}${hash}${hash} How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.15.0

${hash} If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```
## REDSCAN-NUCLEI-VULNERABILITY

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | HTTP_SERVICE                                 |
| Send to       |                                              |
| Tools used    | Nuclei                                       |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Check vulnerabilities using nuclei vulnerability tags.

### Description

Check vulnerabilities using nuclei vulnerability tags and insert vulnerabilities according to the template severity

### How to develop

```
docker login https://ghcr.io
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1

# If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```
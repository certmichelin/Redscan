## REDSCAN-CACHE

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  |                                              |
| Send to       |                                              |
| Tools used    |                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Manage the cache of all scanners.

### Description

**/!\ Cache Key cannot exceed 2000 char!** 
The cache manager will flush all cache entries older than one week.

### How to develop

```
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```

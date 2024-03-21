## REDSCAN-URLCRAZY

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | MASTERDOMAINS                                |
| Send to       | FANOUT_EXCHANGE_VULNERABILITY                |
| Tools used    | urlcrazy 0.7.3                               |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Monitor registered exotic domains name which can be used for Phishing , Url hijacking, typo-squatting and affiliates.

### Description

Brute force domains names using glitched domain provided (ex: nichelin instead od michelin) and requests for them.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1

# If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```

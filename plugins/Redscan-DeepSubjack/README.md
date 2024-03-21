## REDSCAN-DEEPSUBJACK

| Attribute     | Value                                                  |
| ------------- | ------------------------------------------------------ |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME                     |
| Send to       | FANOUT_DOMAINS_EXCHANGE_NAME                           |
| Tools used    | Subjack                                                |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file           |
|               | /root/.config/subfinder : Subfinder YAML configuration |

### Objective

Enumerate subdomains that will not be detected by others subdomains tools which exclude Wilcards and deadomains.

### Description

Combine subfinder with subjack to find potential domain that can be takeovered.

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```
## REDSCAN-SUBFINDER

| Attribute     | Value                                                  |
| ------------- | ------------------------------------------------------ |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME                     |
| Send to       | FANOUT_DOMAINS_EXCHANGE_NAME                           |
| Tools used    | Subfinder 2.4.9                                        |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file           |
|               | /root/.config/subfinder : Subfinder YAML configuration |

### Objective

Subdomain enumeration on Master Domains

### Description

The scanner will use the registered APIs to gather domains through a master domains. Sending found domains to the domains queue

### Environment requirments

Need to have the subfinder.env with filled API Keys. 

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```# Redscan-Subfinder

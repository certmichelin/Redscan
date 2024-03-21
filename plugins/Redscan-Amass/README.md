## REDSCAN-AMASS

| Attribute     | Value                                                  |
| ------------- | ------------------------------------------------------ |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME                     |
| Send to       | FANOUT_DOMAINS_EXCHANGE_NAME                           |
| Tools used    | amass                                                  |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file           |
| Configuration | /wordlist/deepmagic.com-prefixes-top50000.txt : bf list|

### Objective

Search subdomains using amass

### Description
 
Use amass with active/passive option and bruteforce. amass configuration has been limit for time consumption to have more result  -min-for-recursive
 Found subdomains are sent to the domains queue


### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1

# If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```
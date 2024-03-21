## REDSCAN-GITGRABBER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME                 |
| Send to       | FANOUT_ALERTS_EXCHANGE_NAME                  |
| Tools used    |                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

The purpose of this worker is to search for potential secret leaks.

### Description

gitGraber is a tool developed in Python3 to monitor GitHub to search and find sensitive data in real time for different online services such as: Google, Amazon (AWS), Paypal, Github, Mailgun, Facebook, Twitter, Heroku, Stripe, Twilio...

### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1

# If cache is required for the plugin
docker run -d -p 8080:8080 --name redscan-cache-dev docker.pkg.github.com/certmichelin/redscan-cache/redscan-cache:latest
```
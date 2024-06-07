## REDSCAN-SHODAN

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME                 |
| Send to       | FANOUT_SERVICES_EXCHANGE_NAME                |
| Tools used    | Shodan                                       |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Get data gathered by Shodan.io for a specific domain.

### Description

The scanner will use the free API to gather host information (ISP, CNAME, PORTS etc..). The ports fields has analyzed and sent to the Service queue.

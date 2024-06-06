## REDSCAN-MASSCAN

| Attribute     | Value                                                                  |
| ------------- | ---------------------------------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME, FANOUT_IPS_EXCHANGE_NAME                 |
| Send to       | FANOUT_SERVICES_EXCHANGE_NAME                                          |
| Tools used    | masscan 1.3.2                                                          |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file                           |

### Objective

Realize port scan using masscan

### Description

Masscan is used with the nmap top 1000 wordlist.


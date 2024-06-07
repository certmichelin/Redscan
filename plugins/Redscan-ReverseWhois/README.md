## REDSCAN-REVERSEWHOIS

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME           |
| Send to       | 
| Tools used    | amass                                        |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Search for new masterdomains via amass

### Description

Use amass Intelligence option to query several APIs to gather masterdomains alike.
This will check for @masterdomains.com or masterdomains.com information in whois database. 

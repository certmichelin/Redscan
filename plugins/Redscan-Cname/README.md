## REDSCAN-CNAME

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME                 |
| Send to       |                                              |
| Tools used    | dig (dns-utils)                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Retrieve all CNAME for a domain

### Description

Parse the dig command output to retrieve cnames.

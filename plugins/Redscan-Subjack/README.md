## REDSCAN-SUBJACK

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_DOMAINS_EXCHANGE_NAME                 |
| Send to       | FANOUT_VULNERABILITIES_EXCHANGE_NAME         |
| Tools used    | Subjack                                      |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Detect potential subdomain takeover.

### Description

User subjack to determine potential domain takeover.

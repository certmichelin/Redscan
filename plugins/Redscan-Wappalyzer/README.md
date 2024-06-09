## REDSCAN-WAPPALYZER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_HTTP_SERVICES_EXCHANGE_NAME           |
| Send to       | N/A                                          |
| Tools used    | Wappalyzer                                   |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Determine which technologies an HTTP service uses.

### Description

Use Wappalyzer to enumerate the technologies used. The value retrieved is cached for 24 hours.


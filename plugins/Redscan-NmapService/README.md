## REDSCAN-NMAPSERVICE

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_SERVICES_EXCHANGE_NAME                |
| Send to       |                                              |
| Tools used    | nmap 7.94                                  |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Determine nature of exposed services.

### Description

Use NMAP service detection feature to enumerate service exposed. 
The value retrieved is cached for 24 hours.
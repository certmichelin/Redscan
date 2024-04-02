## REDSCAN-ALERTS

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_ALERTS_EXCHANGE_NAME                  |
| Send to       | N/A                                          |
| Tools used    | N/A                                          |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Spread alerts when vulnerabilities are found.

### Description

Send alert to different channel according to the severity, currently it will send alert on Microsoft TEAMS and email.

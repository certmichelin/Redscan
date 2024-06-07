## REDSCAN-SECURITYTXT

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  |         FANOUT_HTTP_SERVICES_EXCHANGE_NAME   |
| Send to       |                    NONE                      |
| Tools used    |                      -                       |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Assert the presence of the security.txt file located as explictly specified by RFC in domain/.well-known/security.txt or at domain/security.txt

### Description

Assert the presence of the security.txt file located as explictly specified by RFC in domain/.well-known/security.txt or at domain/security.txt
Parse the content and assert the expected content.


## REDSCAN-SSLSCAN

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_HTTP_SERVICE_EXCHANGE_NAME            |
| Send to       | FANOUT_VULNERABILITIES_EXCHANGE_NAME         |
| Tools used    | SSLScan 2.0.10                               |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Inspect SSL/TLS configuration (Weak protocols/ciphers) and check Heartbleed vulnerability on HTTPS Service.

### Description

Use SSl scan ot gather intel on certificate and on SSL/TLS. Send vulnerability accordingly:

- Obsolete protocol. eg: SSL
- Obsolete protocol version. eg: TLS1.0
- Heartbleed
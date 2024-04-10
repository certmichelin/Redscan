## REDSCAN-IPRANGESCANNER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_IPRANGES_EXCHANGE_NAME                |
| Send to       | FANOUT_IP_EXCHANGE_NAME                      |
| Tools used    | Plain java + masscan                         |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

The purpose of this worker is to detect online host from IP Range.

### Description

The detection is made in two steps :
1. Ping the IP address.
2. If the ping failed then execute a post scan with masscan to verify is the host is online.


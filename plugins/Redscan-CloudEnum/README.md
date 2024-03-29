## REDSCAN-CLOUDENUM

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_BRANDS_EXCHANGE_NAME                |
| Send to       | FANOUT_VULNERABILITY_EXCHANGE_NAME         |
| Tools used    | cloud_enum fork from initstring              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Lateralize Recon phase in order to obtain new domains using brand name and check associated potential bucket.

### Description

Use fuzzed string to check if a company got open or protected bucket (aws, google, azure) and eventually new domains. 


## REDSCAN-EXPLORETLD

| Attribute     | Value                                            |
| ------------- | ------------------------------------------------ |
| Subscribe to  | FANOUT_BRANDS_EXCHANGE_NAME                      |
| Send to       | N/A                                              |
| Tools used    | dig                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file     |
| Configuration | /wordlists/public_suffix_list.dat : TLD wordlist |

### Objective

Search master domain using TLD wordlist.

### Description

Based on Mozilla TLD worldist, the scanner will try to find master domains. The master domains will be inserted in ES waiting manual validation to be sent in domains queue. 


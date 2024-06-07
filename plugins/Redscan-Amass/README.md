## REDSCAN-AMASS

| Attribute     | Value                                                  |
| ------------- | ------------------------------------------------------ |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME                     |
| Send to       | FANOUT_DOMAINS_EXCHANGE_NAME                           |
| Tools used    | amass v3.23.3                                          |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file           |
| Configuration | /wordlist/deepmagic.com-prefixes-top50000.txt : bf list|

### Objective

Search subdomains using amass

### Description
 
Use amass with active/passive option and bruteforce. amass configuration has been limit for time consumption to have more result  -min-for-recursive
 Found subdomains are sent to the domains queue


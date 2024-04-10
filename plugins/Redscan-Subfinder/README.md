## REDSCAN-SUBFINDER

| Attribute     | Value                                                  |
| ------------- | ------------------------------------------------------ |
| Subscribe to  | FANOUT_MASTERDOMAINS_EXCHANGE_NAME                     |
| Send to       | FANOUT_DOMAINS_EXCHANGE_NAME                           |
| Tools used    | Subfinder 2.6.6                                        |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file           |
|               | /root/.config/subfinder : Subfinder YAML configuration |

### Objective

Subdomain enumeration on Master Domains

### Description

The scanner will use the registered APIs to gather domains through a master domains. Sending found domains to the domains queue

### Environment requirements

Need to have the subfinder.env with filled API Keys. 

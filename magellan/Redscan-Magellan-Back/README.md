## REDSCAN-MAGELLAN-BACK

| Attribute     | Value                                                                                         |
| ------------- | --------------------------------------------------------------------------------------------- |
| Subscribe to  | N/A                                                                                           |
| Send to       | FANOUT_BRANDS_EXCHANGE_NAME,FANOUT_IPRANGES_EXCHANGE_NAME, FANOUT_MASTERDOMAINS_EXCHANGE_NAME |
| Tools used    |                                                                                               |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file                                                  |

### Objective

Magellan-Back project handle all external required interactions between end users and Redscan.

### Description

Redscan REST API.

### How to develop

```bash
docker compose up
```

```http
POST /auth/realms/Redscan/protocol/openid-connect/token HTTP/1.1
Host: localhost
Accept: */*
Content-Type: application/x-www-form-urlencoded
Content-Length: 66
Connection: keep-alive

grant_type=password&client_id=magellan&username=demo&password=demo
```

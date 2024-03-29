## REDSCAN-GITGRABBER

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_BRANDS_EXCHANGE_NAME                 |
| Send to       | FANOUT_ALERTS_EXCHANGE_NAME                  |
| Tools used    |                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

The purpose of this worker is to search for potential secret leaks.

### Description

gitGraber is a tool developed in Python3 to monitor GitHub to search and find sensitive data in real time for different online services such as: Google, Amazon (AWS), Paypal, Github, Mailgun, Facebook, Twitter, Heroku, Stripe, Twilio...


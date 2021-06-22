Redscan
=======

Redscan discovers websites and which services they are running.

```
python red.py -h
```

#### Exposed Service
| Port  | Endpoint  | Service                          | Default credentials |
| :---- | :-------- | :------------------------------- | :------------------ |
| 3000  | /         | Grafana (visualize metrics)      | admin/admin         |
| 443   | /kibana   | Kibana                           |                     |
| 443   | /magellan | Magellan Web application         |                     |
| 443   | /auth     | Keycloack                        |                     |
| 9090  |           | Prometheus (metrics database)    |                     |
| 9091  |           | Prometheus-Pushgateway           |                     |
| 9093  |           | AlertManager (alerts management) |                     |
| 9200  | /         | Elastic Search (Removed soon)    |                     |
| 443   | /rabbitmq | Rabbit MQ Management             | guest/guest         |

#### Integrate Github Package.

As Rescan project used Github Package registry, you need to login to github with docker in order to be able to pull the images.

```
docker login https://docker.pkg.github.com
```

#### Quick start
```
python red.py --install-dockprom
python red.py --setup-demo

#Fill the specific environment with credentials.
python red.py --run
```

** WARNING : --setup demo is unsecure and should not be exposed over the internet. For a full configuration, please refer to the project wiki

Resources
---------
- https://github.com/jhaddix/tbhm
- https://www.youtube.com/watch?v=jHWUkYzMf6k&list=PLUOjNfYgonUtXMr4ljtM9iVUAIPMnD-3Y&index=2
- https://www.youtube.com/watch?v=q11eBk_k6DA&list=PLUOjNfYgonUtXMr4ljtM9iVUAIPMnD-3Y&index=32
- https://www.youtube.com/watch?v=p4JgIu1mceI
- https://pentester.land/cheatsheets/2019/03/25/compilation-of-recon-workflows.html
- https://github.com/BountyMachine
- https://www.hahwul.com/2020/09/23/amass-go-deep-in-the-sea-with-free-apis/

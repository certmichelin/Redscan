<h1 align="center">
  Redscan by <a href="https://cert.michelin.com"><img src="https://cert.michelin.com/img/Logo_MICHELIN_EN.png" width="200px" alt="Redscan"></a>
</h1>

<p align="center">
  <a href="https://github.com/certmichelin/Redscan/wiki">Introduction</a> •
  <a href="https://github.com/certmichelin/Redscan/wiki/Developers">How to develop</a> •
  <a href="https://github.com/certmichelin/Redscan/wiki/Plugin-List">Plugin list</a> •
  <a href="https://github.com/certmichelin/Redscan/wiki/Troubleshooting">FAQs</a>
</p>

<h1></h1>

Redscan is built to discover exposed assets of a company, detect misconfigurations and compliance deviations.

Redscan was conceived with the idea to automate the recon phase and the vulnerability assertion as referred to the Bug Bounty Methodology. 

The aim of the project is to facilitate the orchestration, the integration and the exploitation of results coming from existing good tools. For that [Redscan-Utils](https://github.com/certmichelin/Redscan-Utils) was developed.

The Michelin CERT developed and continue to maintain plenty of plugins covering most of known use-cases. You can find them on https://github.com/certmichelin .

<br/>

# Quick start

As Redscan project used Github Package registry, you need to login to github with docker in order to be able to pull the images.

```
docker login https://ghcr.io
```

Setup a quick demo instance in four commands

```
git clone https://github.com/certmichelin/Redscan.git
cd Redscan
python red.py --install-dockprom
python red.py --setup-demo
```

In order to be more accurate, some plugins required api keys such as subfinder, gitgrabber or alert, you can find them under `conf` folder. Values are surrounded by `§` character.

```
# --demo run one instance per plugin that would cause bottleneck for big scope.
python red.py --demo
```

You can now use `demo/demo` for playing with Redscan and `administrator/redscan` for Mantis BT

**WARNING : --setup demo is unsecure and should not be exposed over the internet. For a full configuration, please refer to the project wiki**

You can display the help command using `python red.py -h`


# Resources

- https://github.com/jhaddix/tbhm
- https://www.youtube.com/watch?v=jHWUkYzMf6k&list=PLUOjNfYgonUtXMr4ljtM9iVUAIPMnD-3Y&index=2
- https://www.youtube.com/watch?v=q11eBk_k6DA&list=PLUOjNfYgonUtXMr4ljtM9iVUAIPMnD-3Y&index=32
- https://www.youtube.com/watch?v=p4JgIu1mceI
- https://pentester.land/cheatsheets/2019/03/25/compilation-of-recon-workflows.html
- https://github.com/BountyMachine
- https://www.hahwul.com/2020/09/23/amass-go-deep-in-the-sea-with-free-apis/

<h1 align="center">
  Redscan by <a href="https://cert.michelin.com">
    <img src="https://raw.githubusercontent.com/certmichelin/Redscan/main/logo.png" width="200"/>
  </a>
</h1>

<h1></h1>

![](https://img.shields.io/github/issues/certmichelin/Redscan.svg)
![](https://img.shields.io/github/forks/certmichelin/Redscan.svg)
![](https://img.shields.io/github/stars/certmichelin/Redscan.svg)
![](https://img.shields.io/github/license/certmichelin/Redscan.svg)

Redscan is built to discover exposed assets of a company, detect misconfigurations and compliance deviations.

Redscan was conceived with the idea to automate the recon phase and the vulnerability assertion as referred to the Bug Bounty Methodology. 

The aim of the project is to facilitate the orchestration, the integration and the exploitation of results coming from existing good tools. For that [Redscan-Utils](https://github.com/certmichelin/Redscan/tree/main/utils/Redscan-Utils) was developed.

The Michelin CERT developed and continue to maintain plenty of plugins covering most of known use-cases. You can find them on https://github.com/certmichelin/Redscan/tree/main/plugins.

<br/>

# Quick start

Setup a quick demo instance in four commands

```
git clone https://github.com/certmichelin/Redscan.git
cd Redscan
python red.py --init demo
python red.py --run normal
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
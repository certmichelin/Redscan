import argparse
import configparser
import os
import sys
import urllib.request

from pathlib import Path
from shutil import rmtree,copyfile,copyfileobj

def init(config_file):
    print("----- Init ENVIRONMENT FILES -----")
    for file in os.listdir("compose/data/environments"):
        if file.endswith(".template") :
            env_file = file.replace(".template", "")
            if not os.path.isfile("compose/data/environments/" + env_file) :
                copyfile("compose/data/environments/" + file, "compose/data/environments/" + env_file)
                print(f"File {env_file} created")
            else :
                print(f"File {env_file} already exists")
    copyfile("compose/.env.template", "compose/.env")
    print(f"File .env created")
    copyfile("compose/conf/redscan-rproxy/redscan.conf.template", "compose/conf/redscan-rproxy/redscan.conf")
    print(f"File compose/conf/redscan-rproxy/redscan.conf created")
    copyfile("compose/conf/keycloak/realm-export.json.template", "compose/conf/keycloak/realm-export.json")
    print(f"File compose/conf/keycloak/realm-export.json created")
    copyfile("compose/conf/redscan-subfinder/app/provider-config.yaml.template", "compose/conf/redscan-subfinder/app/provider-config.yaml")
    print(f"File compose/conf/redscan-subfinder/app/provider-config.yaml created")

    print("")

    print("----- Init WORDLISTS FILES -----") 
    fileToDownload = {
        #Suffix wordlist
        'https://raw.githubusercontent.com/danielmiessler/SecLists/master/Discovery/Infrastructure/nmap-ports-top1000.txt' : 'nmap-ports-top1000.txt'
    }    
    for url in fileToDownload.keys():
        with urllib.request.urlopen(url) as response, open('./compose/data/wordlists/'+fileToDownload[url], 'wb') as out_file:
            copyfileobj(response, out_file)
            print(f"File {fileToDownload[url]} created from {url}")
    print("")

    print("----- Install DOCKPROM -----")
    os.system('git clone https://github.com/stefanprodan/dockprom.git compose/dockprom')
    print("")

    print("----- Install VARIABLES -----")
    if config_file == "demo":
        print("Redscan is configured in demo mode that is not secured. DON'T USE IT IN OTHER ENVIRONMENT!!!")
        config_file = Path('compose/demo/localhost.conf')
        print("")

    try:
        print(f'Using {config_file} as config file')
        with open(config_file) as f:
            config = configparser.ConfigParser()	
            config.read_file(f)
            config.read(config_file,encoding='utf-8')
    except IOError :
        raise Exception('IOError', f'Unable to open config file: {config_file}')

    if config['CONFIG']['KEYCLOACK_FILE']:
        print(f"Copy Keycloak configuration from {config['CONFIG']['KEYCLOACK_FILE']}")
        copyfile(config['CONFIG']['KEYCLOACK_FILE'], "compose/conf/keycloak/realm-export.json")
    else:
        print("No Keycloak configuration found, using default one")
    print("")

    for dname, dirs, files in os.walk(Path("./compose")):
        for fname in files:
            fpath = os.path.join(dname, fname)
            
            if '/.git/' not in fpath and '/dockprom/' not in fpath and '/demo/' not in fpath and '/logs/' not in fpath and '/mantisbt_db/' not in fpath and 'env.template' not in fpath and 'json.template' not in fpath and 'yaml.template' not in fpath and '.DS_Store' not in fpath and  str(config_file) not in fpath:
                print(f'Processing file {fpath}')
                with open(fpath, encoding='utf-8') as f:
                    previousText = f.read()

                    res=previousText
                    for toReplace in config['ENV'].keys():
                        res = res.replace(toReplace.upper(), config['ENV'][toReplace])
                        
                if res != previousText:
                    with open(fpath, "w", encoding='utf-8') as f:
                        f.write(res)
                        print(f'File {fpath} modified')
    print("")

def reset():
    print("----- Reset ENVIRONMENT FILES -----")
    for file in os.listdir("compose/data/environments"):
        if file.endswith(".env") :
            os.remove("compose/data/environments/" + file)
            print(f"File {file} removed")
    os.remove("compose/.env")
    print(f"File .env removed")
    print("")

    print("----- Reset WORDLISTS FILES -----")
    for wordlist in os.listdir("./compose/data/wordlists"):
        wordlist_path = os.path.join("./compose/data/wordlists", wordlist)
        if os.path.isfile(wordlist_path) and '.gitignore' not in wordlist_path and '.gitkeep' not in wordlist_path:
            os.remove(wordlist_path)  
            print(f"Wordlist {wordlist} removed")
    print("")

    print("----- Reset Dockprom -----")
    rmtree("./compose/dockprom")
    print("Directory ./compose/dockprom/ removed")
    print("")

    print("----- Reset Files -----")
    files = ["compose/conf/keycloak/realm-export.json", "compose/conf/redscan-rproxy/redscan.conf", "compose/conf/redscan-subfinder/app/provider-config.yaml"]
    for file in files:
        if os.path.isfile(file):
            os.remove(file)
            print(f"File {file} removed")

def run(mode):
    print("----- Run REDSCAN as Demo -----") if mode == "debug" else print("----- Run REDSCAN -----")
    if mode != "light":
        os.system('docker compose -f compose/dockprom/docker-compose.yml up -d') 
    os.system('docker compose -f compose/docker-compose.yml up') if mode == "debug" else os.system('docker compose -f compose/docker-compose.yml up -d')

def stop():
    print("----- Stop REDSCAN -----")
    os.system('docker compose -f compose/docker-compose.yml stop')
    os.system('docker compose -f compose/dockprom/docker-compose.yml stop')

def down():
    print("----- Clean REDSCAN -----")
    os.system('docker compose -f compose/docker-compose.yml down')
    os.system('docker compose -f compose/dockprom/docker-compose.yml down')

#
# Redscan main method
#
def main():

    print(r"""
================================================================================================
____ ____ ___  ____ ____ ____ _  _    ___  _   _    _  _ _ ____ _  _ ____ _    _ _  _ 
|__/ |___ |  \ [__  |    |__| |\ |    |__]  \_/     |\/| | |    |__| |___ |    | |\ | 
|  \ |___ |__/ ___] |___ |  | | \|    |__]   |      |  | | |___ |  | |___ |___ | | \| 

Under Apache 2.0 License, see https://github.com/certmichelin/Redscan                           
================================================================================================                                                                                                                                                                                       
    """)
    
    parser = argparse.ArgumentParser(description="Redscan - A scalable and flexible security scanning solution")
    parser.add_argument('--init', action="store", dest="config_file", default=None, help="Init Redscan the environment, wordlists and variables using a configuration file. 'demo' value could be used for demo purpose.")
    parser.add_argument('--reset', action="store_true", dest="reset", default=None, help="Reset Redscan. It will reset everything.")
    parser.add_argument('--run', action="store", dest="run", choices=['normal', 'debug', 'light'], help="Run Redscan. If debug is selected, it will run redscan with console output.")
    parser.add_argument('--stop', action="store_true", dest="stop", default=None, help="Stop redscan.")
    parser.add_argument('--down', action="store_true", dest="down", default=None, help="Stop & Remove all redscan containers.")
    params = parser.parse_args()

    if params.config_file:
        init(params.config_file)
    elif params.reset:
        reset()
    elif params.run:
        run(params.run)
    elif params.stop:
        stop()
    elif params.down:
        down()
    else:
        parser.print_help()
    
    sys.exit(0)

if __name__ == '__main__':
    main()

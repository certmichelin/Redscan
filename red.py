import argparse
import os
import sys
import urllib.request
import configparser

from pathlib import Path
from shutil import rmtree,copyfile,copyfileobj


#
# Red main method
#
def main():
    parser = argparse.ArgumentParser(description='Redscan manager')
    parser.add_argument('--init', action="store_true", dest="init", default=None, help="Init everything...")
    parser.add_argument('--init-env', action="store_true", dest="init_env", default=None, help="Initialize env files")
    parser.add_argument('--reset-env', action="store_true", dest="reset_env", default=None, help="Delete env files")
    parser.add_argument('--init-wordlists', action="store_true", dest="init_wordlists", default=None, help="Initialize wordlists files")
    parser.add_argument('--reset-wordlists', action="store_true", dest="reset_wordlists", default=None, help="Delete wordlists files")
    parser.add_argument('--init-backup', action="store_true", dest="init_backup", default=None, help="Initialize backup directory")
    parser.add_argument('--init-variables', action="store_true", dest="init_variables", default=None, help="Initialize variables")
    parser.add_argument('--init-variables-file', action="store", dest="init_variables_file", default=Path('red.conf'), help="Specify configuration file for variables. By default it will use the demo configuration.")
    parser.add_argument('--setup-demo', action="store_true", dest="setup_demo", default=None, help="Setup demo. Not Secured. DON'T USE IT IN OTHER ENVIRONMENT")
    parser.add_argument('--install-dockprom', action="store_true", dest="install_dockprom", default=None, help="Install dockprom monitoring.")
    parser.add_argument('--update-dockprom', action="store_true", dest="update_dockprom", default=None, help="Update dockprom monitoring.")
    parser.add_argument('--run', action="store_true", dest="run", default=None, help="Run redscan with monitoring")
    parser.add_argument('--debug', action="store_true", dest="debug", default=None, help="Run redscan with console output.")
    parser.add_argument('--stop', action="store_true", dest="stop", default=None, help="Stop redscan.")
    parser.add_argument('--down', action="store_true", dest="down", default=None, help="Stop & Remove all redscan containers.")
    params = parser.parse_args()

    print("""
                    Redscan by Michelin CERT
    """)

    #Init environment file.
    if params.init_env or params.init or params.setup_demo:
        print("--- Init ENVIRONMENT FILES ---")
        for file in os.listdir("data/environments"):
            if file.endswith(".template") :
                env_file = file.replace(".template", "")
                if not os.path.isfile("data/environments/" + env_file) :
                    copyfile("data/environments/" + file, "data/environments/" + env_file)
       
    #Reset environment file.
    if params.reset_env:
        print("--- Reset ENVIRONMENT FILES ---")
        for file in os.listdir("data/environments"):
            if file.endswith(".env") :
                os.remove("data/environments/" + file)

    #Init wordlist files.
    if params.init_wordlists or params.init or params.setup_demo:
        print("--- Init WORDLISTS FILES ---")
        
        fileToDownload = {
            #Suffix wordlist
            'https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat' : 'public_suffix_list.dat',
            'https://raw.githubusercontent.com/danielmiessler/SecLists/master/Discovery/Infrastructure/nmap-ports-top1000.txt' : 'nmap-ports-top1000.txt',
            'https://raw.githubusercontent.com/danielmiessler/SecLists/master/Discovery/DNS/deepmagic.com-prefixes-top50000.txt' : 'deepmagic.com-prefixes-top50000.txt',
        }

        for url in fileToDownload.keys():
            with urllib.request.urlopen(url) as response, open('./data/wordlists/'+fileToDownload[url], 'wb') as out_file:
                copyfileobj(response, out_file)
    
    if params.setup_demo:
        #Init variables for localhost
        print("Setup demo. Not Secured. DON'T USE IT IN OTHER ENVIRONMENT")
        copyfile("demo/demo-export.json", "conf/keycloak/realm-export.json")
        params.init_variables_file = Path('demo/localhost.conf')

    if params.init_variables or params.init or params.setup_demo:
        print("--- Init VARIABLES ---")
        try:
            print(f'Using {params.init_variables_file} as config file')
            with open(params.init_variables_file) as f:
                config = configparser.ConfigParser()	
                config.read_file(f)
                config.read(params.init_variables_file,encoding='utf-8')
        except IOError :
            raise Exception('IOError', f'Unable to open config file: {params.init_variables_file}')

        for dname, dirs, files in os.walk(Path("./")):
            for fname in files:
                fpath = os.path.join(dname, fname)
                
                if '.git' not in fpath and 'dockprom' not in fpath and  str(params.init_variables_file) not in fpath:
                    with open(fpath, encoding='utf-8') as f:
                        previousText = f.read()

                        res=previousText
                        for toReplace in config['CONFIG'].keys():
                            res = res.replace(toReplace.upper(), config['CONFIG'][toReplace])
                            
                    if res != previousText:
                        with open(fpath, "w", encoding='utf-8') as f:
                            f.write(res)
                            print(f'File {fpath} modified')

    #Reset wordlist files
    if params.reset_wordlists:
        print("--- Reset WORDLISTS FILES ---")
        rmtree("./data/wordlists") 

    #Init backup directory
    if params.init_backup or params.init:
        print("--- Creating Backup directory ---")
        os.makedirs('backup', mode=0o766, exist_ok=True)
        os.chmod('backup',0o766)

    #Install dockprom.
    if params.install_dockprom:
        print("--- Install DOCKPROM ---")
        os.system('git clone https://github.com/stefanprodan/dockprom.git')

    #Update dockprom.
    if params.update_dockprom:
        print("--- Update DOCKPROM ---")
        os.system('git -C dockprom pull')
        
    #Run.
    if params.run:
        print("--- Run REDSCAN ---")
        os.system('docker-compose -f dockprom/docker-compose.yml up -d')
        #Don't forget to uncomment this line when amass will be reenabled.
        #os.system('docker-compose up --scale redscan-nmapservice=5 --scale redscan-amass=3 --scale redscan-masscan=2 -d')
        os.system('docker-compose up --scale redscan-nmapservice=5 --scale redscan-nuclei-exposedpanels=5 --scale redscan-nuclei-exposure=5 --scale redscan-masscan=2 -d')

    #Debug.
    if params.debug:
        print("--- Debug REDSCAN ---")
        os.system('docker-compose up')

    #Stop.
    if params.stop:
        print("--- Stop REDSCAN ---")
        os.system('docker-compose stop')
        os.system('docker-compose -f dockprom/docker-compose.yml stop')

    #Down.
    if params.down:
        print("--- Clean REDSCAN ---")
        os.system('docker-compose down')
        os.system('docker-compose -f dockprom/docker-compose.yml down')

    sys.exit(0)

if __name__ == '__main__':
    main()

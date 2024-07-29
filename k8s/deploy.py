import argparse
import os
import sys
import subprocess


#
#  Check if Azure CLI is installed.
#
def check_az_installed():
    try:
        subprocess.run(['az', '--version'], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False
    
#
#  Check if Ansible is installed.
#
def check_ansible_installed():
    try:
        subprocess.run(['ansible', '--version'], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False
    
#
#  Check if Azure AzCollection is installed.
#
def check_azcollection_installed():
    try:
        result = subprocess.run(['ansible-galaxy', 'collection', 'list', 'azure.azcollection'], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        output = result.stdout.decode('utf-8').strip()
        if output:
            return True
        else:
            return False
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False
    
#
# Check if az cli command is logged.
#
def check_az_logged():
    try:
        subprocess.run(['az', 'account', 'show'], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except subprocess.CalledProcessError:
        return False

#
# Generate SSH key pair
#
def generate_ssh_key_pair():
    try:
        folder_path = os.path.join(os.getcwd(), './target/ssh')
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        
        # Generate the SSH key pair
        private_key_path = os.path.join(folder_path, 'redscan')
        public_key_path = os.path.join(folder_path, 'redscan.pub')
        
        subprocess.run(['ssh-keygen', '-t', 'rsa', '-b', '4096', '-N', '', '-f', private_key_path], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        print(f"SSH key pair generated successfully. Private key: {private_key_path}, Public key: {public_key_path}")
        return True
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False

#
# Check all prequisites for Redscan-K8S.
#
def check():
    success = True
    if not check_az_installed():
        print("Error - Azure CLI is not installed. Please install Azure CLI before running this script.")
        success = False
    if not check_az_logged():
        print("Error - Azure CLI is not logged in. Please login to Azure CLI before running this script.")
        success = False
    if not check_ansible_installed():
        print("Error - Ansible is not installed. Please install Ansible before running this script.")
        success = False
    if not check_azcollection_installed():
        print("Error - Azure AzCollection is not installed. Please install Azure AzCollection before running this script.")
        success = False
    return success

#
# Prepare all elements required for provisionning Redscan-K8S.
#
def prepare():
    success = True
    if not generate_ssh_key_pair():
        print("Error - SSH key pair generation failed.")
        success = False
    return success

#
# Redscan main method
#
def main():

    print("""
=========================================================================================================
____ ____ ___  ____ ____ ____ _  _    _  _ ____ ____    ___  _   _    _  _ _ ____ _  _ ____ _    _ _  _ 
|__/ |___ |  \ [__  |    |__| |\ | __ |_/  |__| [__     |__]  \_/     |\/| | |    |__| |___ |    | |\ | 
|  \ |___ |__/ ___] |___ |  | | \|    | \_ |__| ___]    |__]   |      |  | | |___ |  | |___ |___ | | \| 
                                                                                                   

Under Apache 2.0 License, see https://github.com/certmichelin/Redscan                           
=========================================================================================================                                                                                                                                                                                       
    """)
    
    parser = argparse.ArgumentParser(description="Redscan - A scalable and flexible security scanning solution")
    parser.add_argument('--check', action="store_true", dest="check", default=None, help="Check Redscan-K8S prerequisites.")
    parser.add_argument('--prepare', action="store_true", dest="prepare", default=None, help="Prepare Redscan-K8S prerequisites.")
    params = parser.parse_args()
    
    if params.check:
        if not check() :
            sys.exit(1)

    if params.prepare:
        if not prepare() :
            sys.exit(1)
    
    sys.exit(0)


if __name__ == '__main__':
    main()
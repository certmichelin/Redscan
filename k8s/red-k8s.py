import argparse
import os
import requests
import sys
import subprocess
import yaml

#
# Generate SSH key pair
#
def prepare_ssh_key_pair():
    try:
        folder_path = os.path.join(os.getcwd(), './target/ssh')
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        else :
            if os.path.exists(os.path.join(folder_path, 'redscan')) and os.path.exists(os.path.join(folder_path, 'redscan.pub')):
                print(f"SSH key pair already exists. Private key: {os.path.join(folder_path, 'redscan')}, Public key: {os.path.join(folder_path, 'redscan.pub')}")
                return True
        
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
# Install Ansible and Azure collection
#     
def prepare_ansible_environment():
    try:
        # Install or upgrade ansible
        subprocess.run(['pip', 'install', 'ansible'], check=True)
        
        # Download requirements-azure.txt
        try:
            response = requests.get('https://raw.githubusercontent.com/ansible-collections/azure/dev/requirements.txt')
            response.raise_for_status()  # Raise an error for bad status codes
            with open("target/requirements-azure.txt", 'wb') as file:
                file.write(response.content)
        except requests.RequestException as e:
            print(f"An error occurred while downloading the azure requirement file")
            return False
        
        # Install requirements from requirements-azure.txt
        subprocess.run(['pip', 'install', '-r', 'target/requirements-azure.txt'], check=True)
        
        # Install azure.azcollection
        subprocess.run(['ansible-galaxy', 'collection', 'install', 'azure.azcollection'], check=True)
        
        print("Ansible and Azure collection installed successfully.")
        return True
    except subprocess.CalledProcessError as e:
        print(f"An error occurred: {e}")
        return False


#
# Prepare inventory file.
#
def prepare_inventory():
    try:
        with open('playbooks/vars/redscan.yaml', 'r') as file:
            redscan_vars = yaml.safe_load(file)
            az_resourcegroup = redscan_vars.get('az_resourcegroup')
            
            folder_path = os.path.join(os.getcwd(), './target/inventory')
            if not os.path.exists(folder_path):
                os.makedirs(folder_path)
            
            inventory_path = os.path.join(os.getcwd(), 'target/inventory/redscan_inventory.azure_rm.yaml')
            with open(inventory_path, 'w') as inventory_file:
                inventory_file.write(f"""
plugin: azure_rm
include_vm_resource_groups:
    - "{az_resourcegroup}"
keyed_groups:
    - key: tags
auth_source: cli
            """)
            print(f"Inventory file created successfully: {inventory_path}")
            return True
    except FileNotFoundError:
        print("Error - redscan.yaml file not found.")
        return False
    except yaml.YAMLError:
        print("Error - Failed to parse redscan.yml file.")
        return False
    

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
# Provision standalone servers
#     
def provision_standalone_server():
    try:
        # Provision Azure resources
        print("Provisioning standalone Azure resources...")
        with open("./logs/provision.log", 'a') as log_file:
            subprocess.run(
                ['ansible-playbook', 'playbooks/provision.yaml'],
                stdout=log_file,
                stderr=log_file
            )
            return True
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False
    

#
# Install standalone services
#     
def install_standalone_service():
    try:
        # Provision Azure resources
        print("Install standalone services...")
        with open("./logs/install.log", 'a') as log_file:
            subprocess.run(
                ['ansible-playbook', 'playbooks/install.yaml', '-i', 'target/inventory/redscan_inventory.azure_rm.yaml', '-u', 'redscan','--private-key', 'target/ssh/redscan'],
                stdout=log_file,
                stderr=log_file
            )
            return True
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False
    

#
# Destroy Azure resources
#     
def destroy_azure_resources():
    try:
        # Destroy Azure resources
        print("Destroy Azure resource group...")
        with open("./logs/destroy.log", 'a') as log_file:
            subprocess.run(
                ['ansible-playbook', 'playbooks/destroy.yaml'],
                stdout=log_file,
                stderr=log_file
            )
            return True
    except FileNotFoundError:
        return False
    except subprocess.CalledProcessError:
        return False


#
# Prepare all elements required for provisionning Redscan-K8S.
#
def prepare():
    success = True
    if not prepare_ssh_key_pair():
        print("Error - SSH key pair generation failed.")
        success = False
    if not prepare_ansible_environment():
        print("Error - Ansible and Azure collection installation failed.")
        success = False
    if not prepare_inventory():
        print("Error - Inventory file creation failed.")
        success = False
    return success

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
# Provision all Azure resources required for Redscan-K8S.
#
def provision():
    success = True
    if not provision_standalone_server():
        print("Error - Provisioning standalone servers failed.")
        success = False
    return success


#
# Install all services required for Redscan-K8S.
#
def install():
    success = True
    if not install_standalone_service():
        print("Error - Install standalone services failed.")
        success = False
    return success


#
# Destroy all Azure resources created for Redscan-K8S.
#
def destroy():
    success = True
    if not destroy_azure_resources():
        print("Error - Destroying Azure resources failed.")
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
    parser.add_argument('--prepare', action="store_true", dest="prepare", default=None, help="Prepare Redscan-K8S prerequisites.")
    parser.add_argument('--check', action="store_true", dest="check", default=None, help="Check Redscan-K8S prerequisites.")
    parser.add_argument('--provision', action="store_true", dest="provision", default=None, help="Provision Azure resources.")
    parser.add_argument('--install', action="store_true", dest="install", default=None, help="Install services.")
    parser.add_argument('--destroy', action="store_true", dest="destroy", default=None, help="Destroy Azure resources.")
    params = parser.parse_args()
    
    if params.check:
        if not check() :
            sys.exit(1)

    if params.prepare:
        if not prepare() :
            sys.exit(1)

    if params.provision:
        if not provision() :
            sys.exit(1)

    if params.install:
        if not install() :
            sys.exit(1)

    if params.destroy:
        if not destroy() :
            sys.exit(1)
    
    sys.exit(0)


if __name__ == '__main__':
    main()
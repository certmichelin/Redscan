# Redscan-Utils

Redscan utils is the main component used by REDSCAN scanners, it will include all commons features to interacts with REDSCAN ecosystem such as ElasticSearch etc...

## Release note

### 7.0.0
   - Publish to Maven Central.
   
### 6.0.6
   - Add sorting for DatalakeStorageItems.

### 6.0.5
   - Add pagination for DatalakeStorageItems.
     
### 6.0.4
   - Integrate MantisBT update.

### 6.0.3
   - Refactorize data attribute in DatalakeStorageItem for Jackson serialization.

### 6.0.2
   - Mutualize LastScanDate attribute on DatalakeStorageItem.

### 6.0.1
   - Apply MantisBT customization.

### 6.0.0
   - Migrate to ElasticSearch 8.8.x
   - Include MantisBT client.

### 5.1.0
   - Add Block list feature. 

### 5.0.2
   - Make ServiceLevel an enumeration.

### 5.0.1
   - Add Vulnerability as DatalakeStorageItem.

### 5.0.0
   - Move to deddobifu.

### 4.2.2
   - Fix the command executor for application that read the outputsteam.

### 4.2.1
   - Log4shell.
   - Description in ip ranges.
   - Service level for brands, ip ranges & master domains

### 4.2.0
   - Integrate MantisBT.

### 4.1.0
   - Include IpRange and Ip.

### 4.0.2
   - Vulnerabilities:FromDatalake fix.

### 4.0.1
   - Several fixes.

### 4.0.0
   - Standardize models.

### 3.1.0
   - Add protocol (tcp,udp,...) in services signature. 

### 3.0.0
   - Open Source !

### 2.0.7
   - Generate URL from HTTP_services.

### 2.0.6
   - HTTP Services refactoring.

### 2.0.5
   - Include network utils : Check if domain resolve to localhost.

### 2.0.4
   - Include vulnerabilities management.

### 2.0.3
   - Add custom exception for Datalake Storage.

### 2.0.2
   - Add directory context for command execution.

### 2.0.1
   - Reinforce the object creation process for elastic search.
   - Retry on conflict for upsertField and deleteField.

### 2.0.0
   - Add timestamp on create, deleteField and upsertField operation.
   - Add parent field in all entities except brands.
   - Add default values for master domains creation.

### 1.1.2
   - Add name, tunnel in Service class.
   - Safer JSON management for models

### 1.1.1
   - Search with criteria.
   - Add HTTP Service.

### 1.1.0
   - Cache Manager.

## Configure Maven to fetch the library.

Authenticating with a personal access token
You must use a personal access token with the appropriate scopes to publish and install packages in GitHub Packages. For more information, see "About GitHub Packages."

You can authenticate to GitHub Packages with Apache Maven by editing your ~/.m2/settings.xml file to include your personal access token. Create a new ~/.m2/settings.xml file if one doesn't exist.

In the servers tag, add a child server tag with an id, replacing USERNAME with your GitHub username, and TOKEN with your personal access token.

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>redscan-utils</id>
            <username>USERNAME</username>
            <password>TOKEN</password>
        </server>
    </servers>
</settings>
```

## How to develop
```
docker login https://docker.pkg.github.com -u USERNAME -p TOKEN
docker compose up -d
```

# Redscan-Utils

Redscan utils is the main component used by REDSCAN scanners, it will include all commons features to interacts with REDSCAN ecosystem such as ElasticSearch etc...

## Release note

### 1.0.1
   - Upgrade mantisbt client to 2.26.2.

### 1.0.0
   - Publish to Maven Central.
   
### Old
   - Add sorting for DatalakeStorageItems.
   - Add pagination for DatalakeStorageItems.
   - Integrate MantisBT update.
   - Refactorize data attribute in DatalakeStorageItem for Jackson serialization.
   - Mutualize LastScanDate attribute on DatalakeStorageItem.
   - Apply MantisBT customization.
   - Migrate to ElasticSearch 8.8.x
   - Include MantisBT client.
   - Add Block list feature. 
   - Make ServiceLevel an enumeration.
   - Add Vulnerability as DatalakeStorageItem.
   - Move to deddobifu.
   - Fix the command executor for application that read the outputsteam.
   - Log4shell.
   - Description in ip ranges.
   - Service level for brands, ip ranges & master domains
   - Integrate MantisBT.
   - Include IpRange and Ip.
   - Vulnerabilities:FromDatalake fix.
   - Several fixes.
   - Standardize models.
   - Add protocol (tcp,udp,...) in services signature. 
   - Open Source !
   - Generate URL from HTTP_services.
   - HTTP Services refactoring.
   - Include network utils : Check if domain resolve to localhost.
   - Include vulnerabilities management.
   - Add custom exception for Datalake Storage.
   - Add directory context for command execution.
   - Reinforce the object creation process for elastic search.
   - Retry on conflict for upsertField and deleteField.
   - Add timestamp on create, deleteField and upsertField operation.
   - Add parent field in all entities except brands.
   - Add default values for master domains creation.
   - Add name, tunnel in Service class.
   - Safer JSON management for models
   - Search with criteria.
   - Add HTTP Service.
   - Cache Manager.

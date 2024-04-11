## Redscan-Archetype

Generate an empty project for Redcan plugin.

### How to use it
Now you can generate the empty project using the following command.

```
mvn archetype:generate -DarchetypeGroupId="red.deddobifu" -DarchetypeArtifactId="redscan-archetype" -DarchetypeVersion="1.0.0-SNAPSHOT" -DgroupId="com.michelin.cert.redscan" -DartifactId="redscan-toto" -Dversion="1.0-SNAPSHOT" -DinteractiveMode=false
```

The option `-Dredscan-util-version="XXX"` can be used to specify the redscan utils version.

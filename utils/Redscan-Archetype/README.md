## Redscan-Archetype

Generate an empty project for Redcan.

### How to use it

First, you need to update your `~/.m2/settings.xml` and add the following repository, the id must not be modified.

Example to add the repository with a profile

```
<activeProfiles>
    <activeProfile>github</activeProfile>
</activeProfiles>
<profiles>
    <profile>
        <id>github</id>
        <repositories>
            <repository>
                <id>archetype</id>
                <name>archetype</name>
                <url>https://maven.pkg.github.com/Deddobifu/Redscan-Archetype</url>
            </repository>
        </repositories>
    </profile>
</profiles>
```

Add the credential in server.

```
<servers>
    ... Other configurations ...
    <server>
        <id>archetype</id>
        <username>USERNAME</username>
        <password>TOKEN</password>
    </server>
</servers>
```

Now you can generate the empty project using the following command.

```
mvn archetype:generate -DarchetypeGroupId="com.michelin.cert.redscan" -DarchetypeArtifactId="redscan-archetype" -DarchetypeVersion="5.0.1" -DgroupId="com.michelin.cert.redscan" -DartifactId="redscan-shodan" -Dversion="1.0-SNAPSHOT" -DinteractiveMode=false
```

The option `-Dredscan-util-version="XXX"` can be used to specify the redscan utils version.

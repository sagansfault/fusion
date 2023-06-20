# Fusion 🟠
A common collection of utilities and structures used across Projecki projects.
There are multiple modules to `fusion` that can be used for different plaforms. Avaliable platforms are:
<br>
- **Core** `[fusion-core]`: The utilities and code in here are applicable to all types of projects. If you just wish to use utility classes and other features
  like redis and redis pubsub messages, this will work for you.
- **Paper** `[fusion-paper]`: This module **contains utilities from core** as well as paper specific utilities like command frameworks and other paper-server
  specific features
- **Velocity** `[fusion-velocity]`: Similar to *fusion-paper*, contains the utilities from core as well as velocity specific features like an extra
  command framework and others
- **Service** `[fusion-service]`: This module also contains utilities from core as well as addtional classes and features to build stand-alone applications.
  <br>

## Using fusion in your projects
**Do not shade these modules into your paper/velocity plugins**. These contain plugin features and work as a core. These modules should be present
in the plugins folder (paper/velocity modules) and depended on via the dependency checks and management done by the platform (plugin.yml for paper and other for velocity)
To get starting using `fusion`, first choose the appropriate platform you are planning to write the project for. 


Simply clone the project, run `mvn clean package install` and add the appropriate module as a dependency
```xml
<dependencies>
    <dependency>
        <groupId>com.projecki</groupId>
        <artifactId>fusion-paper</artifactId> 
        <!--Replace paper with your desired module (core, paper, velocity)-->
        <version>2.0-SNAPSHOT</version> <!--Check for recent version-->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Contributions
Additions can be made in the way of pull requests. Do not submit a large PR that has not been tested against the new features or affected current features.

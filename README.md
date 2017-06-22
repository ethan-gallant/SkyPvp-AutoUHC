# SkyPVP-AutoUHC
Originally developed and maintained by [xMakerx](https://github.com/xMakerx).

## Maintenance and Future Development by Third-Parties
Because another developer may want to make changes to the plugin's code, **SkyPVP-AutoUHC** features in-depth documentation and
comments in *almost* every single class.

## How to Build
*THIS PROJECT WAS ORIGINALLY BUILT WITH JAVA 1.8.0_131*
**NOTE: Make sure you have [Apache Maven](https://maven.apache.org/) downloaded and installed correctly.**

1. Verify that you have *all* unmanaged dependencies deployed to your local Maven repository.
  - There's instructions on how to do this inside of [pom.xml](https://github.com/Volxz/SkyPvp-AutoUHC/blob/master/pom.xml).
2. Verify that you have the Java Development Kit (**JDK**) and it's *at least* version 1.8.
3. Open up your System's Terminal and change your working directory to the project's base directory.
4. Run one of the following commands to build the project:
  > mvn package - Simply packages and creates two jars. **USE WHEN WORKING OFFLINE**
  > mvn clean package - Cleans up the /target directory, redownloads all dependencies and Maven plugins, then builds.
5. Place the shaded jar inside of your server's /plugins directory.
